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
	 * ��������ά��
	 */
	private int dimension;

	/**
	 * ���������
	 */
	private Group[] groups;

	/**
	 * ��������������
	 */
	private List<SamplePoint> points;

	/**
	 * ������
	 */
	private int k;

	private int minNumThres;

	private float std_deviationThres;

	private float minDistanceThres;

	private int maxMergeNumsThres;

	/**
	 * ȫ��ģʽ����������Ӧ�������ĵ���ƽ������
	 */
	private float totalMeanDistance;

	/**
	 * ����������
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
	 * ����K��ֵ�����㷨
	 */
	public void run() {
		int nIters = 1;
		while (nIters <= maxIters) {
			System.out.println("--------��������ţ�" + nIters + "-------");

			// ��2������ģʽ�����������ԭ����䵽����������
			assignGroups();

			// ��ӡ�������
			printGroups();

			// ��3����ȥ��������Ŀ���ٵľ��ࣨС��ĳһ����ֵ��
			purgeGroups();

			// ��4��6���������������ġ������������ƽ�������Լ�ȫ��ģʽ����������Ӧ�������ĵ���ƽ������
			updateMeans();

			// ��7�����ж��Ƿ���Ҫ����
			if (nIters == maxIters) {
				// ���һ�ε�������������Ѵ���
				minDistanceThres = 0.0f;
			} else if (groups.length <= (this.k / 2)
					|| (nIters % 2 == 1 && groups.length < (this.k * 2))) {
				// �������ĵ���Ŀ���ڻ򲻵�Ԥ��ֵ��һ�룬���߼Ȳ���ż�ε������������ĵ���ĿҲ�����ڻ����Ԥ��ֵ��������������Ѵ���
				// ��8��10�������Ѵ���
				boolean splitted = splitGroups();
				if (splitted) {
					// ������ɣ����ص�2��
					nIters++;
					continue;
				}
			}

			// ��11��13��
			mergeGroups();

			nIters++;
		}

		System.out.println("-------------------------���ս��----------------------------");

		assignGroups();

		// ��ӡ�������
		printGroups();
	}

	private void assignGroups() {
		Iterator<SamplePoint> iterPoints = this.points.iterator();
		while (iterPoints.hasNext()) {
			SamplePoint point = iterPoints.next();

			int nearest = -1;
			float minDistance = Float.MAX_VALUE;

			// Ѱ���������ľ���
			for (int i = 0; i < groups.length; i++) {
				float distance = SamplePoint.distance(point, groups[i].getMeanPoint());
				if (distance < minDistance) {
					minDistance = distance;
					nearest = i;
				}
			}

			StringBuffer sbuf = new StringBuffer(point + " ==> " + groups[nearest]);
			sbuf.append(", ��С����=" + minDistance);
			System.out.println(sbuf);

			point.setGroupNumber(nearest);
		}
	}

	private void purgeGroups() {
		// ������ͬһ����������������һ��
		Map<Integer, List<SamplePoint>> mapGroup2Samples = getMapGroup2Samples();

		// �����ϸ����������϶ࣩ�ľ���
		List<Group> eligibleGroups = new ArrayList<Group>();
		for (int i = 0; i < groups.length; i++) {
			List<SamplePoint> groupPoints = mapGroup2Samples.get(i);
			if (groupPoints.size() >= minNumThres) {
				eligibleGroups.add(groups[i]);
			} else {
				System.out.println("ȡ�����ࣺ" + groups[i]);
				// ����Щ�������������������Ϊ-1����ʾ�������κξ���
				for (SamplePoint point : groupPoints) {
					point.setGroupNumber(-1);
				}
			}
		}

		// ���ڿ�����Щ���౻ɾ������Ҫ���¾���Ĵ����Լ��������е������������
		for (int i = 0; i < eligibleGroups.size(); i++) {
			// �µľ������Ϊ��i
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
	 * ���Ѵ���
	 */
	private boolean splitGroups() {

		System.out.println("������Ѵ���...");

		boolean splitted = false;

		List<Group> groupList = new ArrayList<Group>();
		// ������ͬһ����������������һ��
		Map<Integer, List<SamplePoint>> mapGroup2Samples = getMapGroup2Samples();
		for (int i = 0; i < groups.length; i++) {
			// �����׼������
			List<SamplePoint> groupPoints = mapGroup2Samples.get(i);
			float[] meanVals = groups[i].getMeanPoint().getValues();
			float[] sum = new float[this.dimension];

			for (SamplePoint point : groupPoints) {
				float[] values = point.getValues();
				for (int j = 0; j < this.dimension; j++) {
					sum[j] += (values[j] - meanVals[j]) * (values[j] - meanVals[j]);
				}
			}

			StringBuffer sbuf = new StringBuffer("groups[" + (i + 1) + "]�ı�׼������Ϊ��(");
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
			sbuf.append("), ������Ϊ��" + maxItem + ", ");
			System.out.println(sbuf);

			boolean flag = false;
			if (maxItem > std_deviationThres) {
				if (groups.length <= this.k / 2
						|| (groups[i].getMeanDistance() > totalMeanDistance && groupPoints
								.size() > 2 * minNumThres)) {
					// ����
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
							.println((groups[i] + " ����Ϊ��" + group1 + "---��---" + group2));
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
			System.out.println("�������������㣡");
		}

		return splitted;
	}

	private void mergeGroups() {

		System.out.println("����ϲ�����...");

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

		// �����������֮��ľ���
		List<GroupDistance> groupDistances = new ArrayList<GroupDistance>();
		for (int i = 0; i < groups.length - 1; i++) {
			Group iGroup = groups[i];
			for (int j = i + 1; j < groups.length; j++) {
				Group jGroup = groups[j];
				float distance = SamplePoint.distance(iGroup.getMeanPoint(), jGroup
						.getMeanPoint());
				System.out.println("����" + i + " �� ����" + j + " �ľ���Ϊ " + distance);
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

		// ��С��������
		Collections.sort(groupDistances);

		StringBuffer sbuf = new StringBuffer("�����������");
		for (GroupDistance distance : groupDistances) {
			sbuf.append("D(" + distance.from + ", " + distance.to + ")="
					+ distance.distance + "  ");
		}
		System.out.println(sbuf);

		List<Group> groupList = new LinkedList<Group>(Arrays.asList(groups));
		// ������ͬһ����������������һ��
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

				System.out.println((group1 + "---��---" + group2 + " �ϲ�Ϊ��" + group));
			}
		}

		for (int i = 0; i < groupList.size(); i++) {
			groupList.get(i).setNumber(i);
		}

		this.groups = new Group[groupList.size()];
		groupList.toArray(this.groups);
	}

	private void printGroups() {
		StringBuffer sbuf = new StringBuffer("�������������");
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
	 * ���µ�ǰ������ľ�ֵ����
	 * 
	 * @return ��û�и����򷵻�false���и����򷵻�true
	 */
	private void updateMeans() {
		StringBuffer sbuf = new StringBuffer("���¾���ľ�ֵ��Ϊ��");

		int pointCount = 0;
		float totalDistance = 0.0f;

		// ������ͬһ����������������һ��
		Map<Integer, List<SamplePoint>> mapGroup2Samples = getMapGroup2Samples();
		for (int i = 0; i < groups.length; i++) {
			List<SamplePoint> groupPoints = mapGroup2Samples.get(i);

			if (groupPoints.size() != 0) {
				float[] values = new float[this.dimension];

				// �ۼ�
				for (SamplePoint point : groupPoints) {
					float[] vv = point.getValues();
					for (int j = 0; j < this.dimension; j++) {
						values[j] += vv[j];
					}
				}

				// ��ƽ��
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

				sbuf.append(groups[i] + ", ����ƽ������(" + meanDistance + "), ");

				pointCount += groupPoints.size();
				totalDistance += groupDistance;
			}
		}

		System.out.println(sbuf.toString());

		totalMeanDistance = (pointCount != 0) ? totalDistance / pointCount : 0.0f;
		System.out.println("��ƽ������ = " + totalMeanDistance);
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

		// ���ó�ʼ������
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

		// ��ʼ����
		ISODATA isodata = new ISODATA(groups, points, k, minNumThres, std_deviationThres,
				minDistanceThres, maxMergeNumsThres, maxIters);
		isodata.run();
	}
}
