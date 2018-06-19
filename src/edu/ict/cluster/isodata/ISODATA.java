package edu.ict.cluster.isodata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;

import edu.ict.Group;
import edu.ict.SamplePoint;
import edu.ict.Utils;

public class ISODATA {

	/**
	 * 特征向量维度
	 */
	private int dimension;

	/**
	 * 保存聚类结果
	 */
	private Group[] groups;

	/**
	 * 保存所有样本点
	 */
	private List<SamplePoint> points;

	/**
	 * 聚类数
	 */
	private int k;

	private int minNumThres;

	private float std_deviationThres;

	private float minDistanceThres;

	private int maxMergeNumsThres;

	/**
	 * 全部模式样本对其相应聚类中心的总平均距离
	 */
	private float totalMeanDistance;

	/**
	 * 最大迭代次数
	 */
	private int maxIters;

	public ISODATA(Group[] groups, List<SamplePoint> points, int k, int minNumThres,
			float std_deviationThres, float minDistanceThres, int maxMergeNumsThres,
			int maxIters) {
		super();
		this.groups = groups;
		this.points = points;
		this.k = k;
		this.minNumThres = minNumThres;
		this.std_deviationThres = std_deviationThres;
		this.minDistanceThres = minDistanceThres;
		this.maxMergeNumsThres = maxMergeNumsThres;
		this.maxIters = maxIters;

		this.dimension = points.get(0).getDimension();
	}

	/**
	 * 运行K均值聚类算法
	 */
	public void run() {
		int nIters = 1;
		while (nIters <= maxIters) {
			System.out.println("--------迭代次序号：" + nIters + "-------");

			// 第2步：将模式样本按最近邻原则分配到各聚类中心
			assignGroups();

			// 打印迭代结果
			printGroups();

			// 第3步：去除样本数目较少的聚类（小于某一个数值）
			purgeGroups();

			// 第4到6步：修正聚类中心、计算聚类类内平均距离以及全部模式样本对其相应聚类中心的总平均距离
			updateMeans();

			// 第7步：判断是否需要分裂
			if (nIters == maxIters) {
				// 最后一次迭代，不进入分裂处理
				minDistanceThres = 0.0f;
			} else if (groups.length <= (this.k / 2)
					|| (nIters % 2 == 1 && groups.length < (this.k * 2))) {
				// 聚类中心的数目等于或不到预定值的一半，或者既不是偶次迭代，聚类中心的数目也不大于或等于预定值的两倍，进入分裂处理
				// 第8到10步：分裂处理
				boolean splitted = splitGroups();
				if (splitted) {
					// 分裂完成，跳回第2步
					nIters++;
					continue;
				}
			}

			// 第11到13步
			mergeGroups();

			nIters++;
		}

		System.out.println("-------------------------最终结果----------------------------");

		assignGroups();

		// 打印迭代结果
		printGroups();
	}

	private void assignGroups() {
		Iterator<SamplePoint> iterPoints = this.points.iterator();
		while (iterPoints.hasNext()) {
			SamplePoint point = iterPoints.next();

			int nearest = -1;
			float minDistance = Float.MAX_VALUE;

			// 寻找最近距离的聚类
			for (int i = 0; i < groups.length; i++) {
				float distance = SamplePoint.distance(point, groups[i].getMeanPoint());
				if (distance < minDistance) {
					minDistance = distance;
					nearest = i;
				}
			}

			StringBuffer sbuf = new StringBuffer(point + " ==> " + groups[nearest]);
			sbuf.append(", 最小距离=" + minDistance);
			System.out.println(sbuf);

			point.setGroupNumber(nearest);
		}
	}

	private void purgeGroups() {
		// 将属于同一个聚类的样本点归在一起
		Map<Integer, List<SamplePoint>> mapGroup2Samples = getMapGroup2Samples();

		// 保留合格（样本数量较多）的聚类
		List<Group> eligibleGroups = new ArrayList<Group>();
		for (int i = 0; i < groups.length; i++) {
			List<SamplePoint> groupPoints = mapGroup2Samples.get(i);
			if (groupPoints.size() >= minNumThres) {
				eligibleGroups.add(groups[i]);
			} else {
				System.out.println("取消聚类：" + groups[i]);
				// 将这些样本点所属聚类代号设为-1，表示不属于任何聚类
				for (SamplePoint point : groupPoints) {
					point.setGroupNumber(-1);
				}
			}
		}

		// 由于可能有些聚类被删除，需要更新聚类的代号以及样本点中的所属聚类代号
		for (int i = 0; i < eligibleGroups.size(); i++) {
			// 新的聚类代号为：i
			Group group = eligibleGroups.get(i);
			List<SamplePoint> groupPoints = mapGroup2Samples.get(group.getNumber());
			for (SamplePoint point : groupPoints) {
				point.setGroupNumber(i);
			}
			group.setNumber(i);
		}

		this.groups = new Group[eligibleGroups.size()];
		eligibleGroups.toArray(this.groups);
	}

	/**
	 * 分裂处理
	 */
	private boolean splitGroups() {

		System.out.println("进入分裂处理...");

		boolean splitted = false;

		List<Group> groupList = new ArrayList<Group>();
		// 将属于同一个聚类的样本点归在一起
		Map<Integer, List<SamplePoint>> mapGroup2Samples = getMapGroup2Samples();
		for (int i = 0; i < groups.length; i++) {
			// 计算标准差向量
			List<SamplePoint> groupPoints = mapGroup2Samples.get(i);
			float[] meanVals = groups[i].getMeanPoint().getValues();
			float[] sum = new float[this.dimension];

			for (SamplePoint point : groupPoints) {
				float[] values = point.getValues();
				for (int j = 0; j < this.dimension; j++) {
					sum[j] += (values[j] - meanVals[j]) * (values[j] - meanVals[j]);
				}
			}

			StringBuffer sbuf = new StringBuffer("groups[" + (i + 1) + "]的标准差向量为：(");
			float[] std_deviationVector = new float[this.dimension];
			int max = -1;
			float maxItem = Float.MIN_VALUE;
			for (int j = 0; j < this.dimension; j++) {
				float std_deviation = (float) Math.sqrt(sum[j] / groupPoints.size());
				std_deviationVector[j] = std_deviation;
				if (std_deviation > maxItem) {
					max = j;
					maxItem = std_deviation;
				}

				// logging...
				sbuf.append(std_deviation);
				if (j < this.dimension - 1)
					sbuf.append(", ");
			}
			sbuf.append("), 最大分量为：" + maxItem + ", ");
			System.out.println(sbuf);

			boolean flag = false;
			if (maxItem > std_deviationThres) {
				if (groups.length <= this.k / 2
						|| (groups[i].getMeanDistance() > totalMeanDistance && groupPoints
								.size() > 2 * minNumThres)) {
					// 分裂
					splitted = true;
					flag = true;

					float delta = 0.5f * maxItem;
					float[] meanVals1 = ArrayUtils.clone(meanVals);
					meanVals1[max] += delta;
					SamplePoint meanPoint1 = new SamplePoint(-1, meanVals1);
					Group group1 = new Group(groupList.size());
					group1.setMeanPoint(meanPoint1);
					groupList.add(group1);

					float[] meanVals2 = ArrayUtils.clone(meanVals);
					meanVals2[max] -= delta;
					SamplePoint meanPoint2 = new SamplePoint(-1, meanVals2);
					Group group2 = new Group(groupList.size());
					group2.setMeanPoint(meanPoint2);
					groupList.add(group2);

					System.out
							.println((groups[i] + " 分裂为：" + group1 + "---和---" + group2));
				}
			}

			if (!flag) {
				groups[i].setNumber(groupList.size());
				groupList.add(groups[i]);
			}

		}

		if (splitted) {
			for (int i = 0; i < groupList.size(); i++) {
				groupList.get(i).setNumber(i);
			}
			this.groups = new Group[groupList.size()];
			groupList.toArray(this.groups);
		} else {
			System.out.println("分裂条件不满足！");
		}

		return splitted;
	}

	private void mergeGroups() {

		System.out.println("进入合并处理...");

		if (groups.length < 2)
			return;

		class GroupDistance implements Comparable<GroupDistance> {
			int from;

			int to;

			float distance;

			public GroupDistance(int from, int to, float distance) {
				this.from = from;
				this.to = to;
				this.distance = distance;
			}

			public int compareTo(GroupDistance that) {
				if (this.distance < that.distance) {
					return -1;
				} else if (this.distance == that.distance) {
					return 0;
				} else {
					return 1;
				}
			}
		}

		// 计算聚类两类之间的距离
		List<GroupDistance> groupDistances = new ArrayList<GroupDistance>();
		for (int i = 0; i < groups.length - 1; i++) {
			Group iGroup = groups[i];
			for (int j = i + 1; j < groups.length; j++) {
				Group jGroup = groups[j];
				float distance = SamplePoint.distance(iGroup.getMeanPoint(), jGroup
						.getMeanPoint());
				System.out.println("聚类" + i + " 和 聚类" + j + " 的距离为 " + distance);
				if (distance < this.minDistanceThres) {
					GroupDistance groupDistance = new GroupDistance(i, j, distance);
					groupDistances.add(groupDistance);
				}
			}
		}
		int size = Math.min(groupDistances.size(), this.maxMergeNumsThres);
		if (size < 1) {
			return;
		}

		// 从小到大排序
		Collections.sort(groupDistances);

		StringBuffer sbuf = new StringBuffer("聚类距离排序：");
		for (GroupDistance distance : groupDistances) {
			sbuf.append("D(" + distance.from + ", " + distance.to + ")="
					+ distance.distance + "  ");
		}
		System.out.println(sbuf);

		List<Group> groupList = new LinkedList<Group>(Arrays.asList(groups));
		// 将属于同一个聚类的样本点归在一起
		Map<Integer, List<SamplePoint>> mapGroup2Samples = getMapGroup2Samples();

		for (int i = 0; i < size; i++) {
			GroupDistance groupDistance = groupDistances.get(i);
			Group group1 = groups[groupDistance.from];
			Group group2 = groups[groupDistance.to];
			int n1 = mapGroup2Samples.get(group1.getNumber()).size();
			int n2 = mapGroup2Samples.get(group2.getNumber()).size();
			int total = n1 + n2;
			if (groupList.contains(group1) && groupList.contains(group2)) {
				groupList.remove(group1);
				groupList.remove(group2);

				float[] meanValues = new float[this.dimension];
				float[] meanValues1 = group1.getMeanPoint().getValues();
				float[] meanValues2 = group2.getMeanPoint().getValues();
				for (int j = 0; j < this.dimension; j++) {
					meanValues[j] = (meanValues1[j] * n1 + meanValues2[j] * n2) / total;
				}
				SamplePoint meanPoint = new SamplePoint(-1, meanValues);
				Group group = new Group(groupList.size());
				group.setMeanPoint(meanPoint);
				groupList.add(group);

				System.out.println((group1 + "---和---" + group2 + " 合并为：" + group));
			}
		}

		for (int i = 0; i < groupList.size(); i++) {
			groupList.get(i).setNumber(i);
		}

		this.groups = new Group[groupList.size()];
		groupList.toArray(this.groups);
	}

	private void printGroups() {
		StringBuffer sbuf = new StringBuffer("样本点分配结果：");
		for (int i = 0; i < groups.length; i++) {
			List<Integer> pointNumbers = new ArrayList<Integer>();
			for (SamplePoint point : this.points) {
				if (point.getGroupNumber() == i) {
					pointNumbers.add(point.getIndex());
				}
			}
			sbuf.append("groups[" + (i + 1) + "]="
					+ ArrayUtils.toString(pointNumbers.toArray()) + ", ");
		}
		System.out.println(sbuf);
	}

	/**
	 * 更新当前各聚类的均值向量
	 * 
	 * @return 若没有更新则返回false，有更新则返回true
	 */
	private void updateMeans() {
		StringBuffer sbuf = new StringBuffer("更新聚类的均值点为：");

		int pointCount = 0;
		float totalDistance = 0.0f;

		// 将属于同一个聚类的样本点归在一起
		Map<Integer, List<SamplePoint>> mapGroup2Samples = getMapGroup2Samples();
		for (int i = 0; i < groups.length; i++) {
			List<SamplePoint> groupPoints = mapGroup2Samples.get(i);

			if (groupPoints.size() != 0) {
				float[] values = new float[this.dimension];

				// 累加
				for (SamplePoint point : groupPoints) {
					float[] vv = point.getValues();
					for (int j = 0; j < this.dimension; j++) {
						values[j] += vv[j];
					}
				}

				// 求平均
				for (int j = 0; j < this.dimension; j++) {
					values[j] /= groupPoints.size();
				}

				SamplePoint meanPoint = new SamplePoint(-1, values);
				meanPoint.setGroupNumber(i);

				groups[i].setMeanPoint(meanPoint);

				float groupDistance = 0.0f;
				for (SamplePoint point : groupPoints) {
					groupDistance += SamplePoint.distance(point, meanPoint);
				}
				float meanDistance = groupDistance / groupPoints.size();
				groups[i].setMeanDistance(meanDistance);

				sbuf.append(groups[i] + ", 类内平均距离(" + meanDistance + "), ");

				pointCount += groupPoints.size();
				totalDistance += groupDistance;
			}
		}

		System.out.println(sbuf.toString());

		totalMeanDistance = (pointCount != 0) ? totalDistance / pointCount : 0.0f;
		System.out.println("总平均距离 = " + totalMeanDistance);
	}

	private Map<Integer, List<SamplePoint>> getMapGroup2Samples() {
		Map<Integer, List<SamplePoint>> mapGroup2Samples = new HashMap<Integer, List<SamplePoint>>();
		for (SamplePoint point : this.points) {
			Utils.hashObject(point.getGroupNumber(), point, mapGroup2Samples);
		}
		return mapGroup2Samples;
	}

	public static void main(String[] args) throws Exception {
		String file = "isodata_data.txt";
		List<SamplePoint> points = Utils.readSamplePoints(file);

		// 设置初始化聚类
		Group[] groups = new Group[2];
		int idx = 0;
		groups[idx] = new Group(idx);
		groups[idx].setMeanPoint(points.get(0));
		idx++;

		groups[idx] = new Group(idx);
		groups[idx].setMeanPoint(points.get(1));
		idx++;

		int k = 3;
		int minNumThres = 1;
		float std_deviationThres = 1;
		float minDistanceThres = 4;
		int maxMergeNumsThres = 1;
		int maxIters = 4;

		// 开始聚类
		ISODATA isodata = new ISODATA(groups, points, k, minNumThres, std_deviationThres,
				minDistanceThres, maxMergeNumsThres, maxIters);
		isodata.run();
	}
}
