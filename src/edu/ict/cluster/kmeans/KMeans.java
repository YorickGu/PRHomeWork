package edu.ict.cluster.kmeans;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

import edu.ict.Group;
import edu.ict.SamplePoint;
import edu.ict.Utils;

/**
 * K均值聚类主程序
 * 
 * @author gyj
 * 
 */
public class KMeans {

	/**
	 * 聚类数
	 */
	private int k;

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

	public KMeans(Group[] groups, List<SamplePoint> points) {
		this.k = groups.length;
		this.groups = groups;
		this.points = points;

		this.dimension = points.get(0).getDimension();

	}

	/**
	 * 运行K均值聚类算法
	 */
	public void run() {
		// 迭代次数
		int nIters = 1;
		do {
			System.out.println("--------迭代次序号：" + nIters + "-------");
			Iterator<SamplePoint> iterPoints = this.points.iterator();
			while (iterPoints.hasNext()) {
				SamplePoint point = iterPoints.next();

				int nearest = point.getGroupNumber();
				float minDistance = SamplePoint.distance(point, this.groups[nearest]
						.getMeanPoint());

				// 寻找最近距离的聚类
				for (int i = 0; i < this.k; i++) {
					float distance = SamplePoint
							.distance(point, groups[i].getMeanPoint());
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

			StringBuffer sbuf = new StringBuffer("本次迭代结果：");
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

			nIters++;

		} while (updateMeans());
	}

	/**
	 * 更新当前各聚类的均值向量
	 * 
	 * @return 若没有更新则返回false，有更新则返回true
	 */
	private boolean updateMeans() {

		boolean reply = false;

		float[][] values = new float[this.k][this.dimension];
		int[] size = new int[this.k];

		SamplePoint[] pastMeans = new SamplePoint[this.k];

		for (int i = 0; i < this.k; i++) {
			pastMeans[i] = groups[i].getMeanPoint();
			values[i] = new float[this.dimension];
		}

		Iterator<SamplePoint> iterPoints = points.iterator();
		while (iterPoints.hasNext()) {

			SamplePoint point = iterPoints.next();
			int clusternumber = point.getGroupNumber();

			float[] _values = point.getValues();
			for (int i = 0; i < _values.length; i++) {
				values[clusternumber][i] += _values[i];
			}
			size[clusternumber]++;

		}

		StringBuffer sbuf = new StringBuffer("更新聚类的均值点为：");

		for (int i = 0; i < this.k; i++) {
			if (size[i] != 0) {
				for (int j = 0; j < this.dimension; j++) {
					values[i][j] = values[i][j] / size[i];
				}

				SamplePoint temp = new SamplePoint(-1, values[i]);
				temp.setGroupNumber(i);

				groups[i].setMeanPoint(temp);
				sbuf.append(groups[i] + ", ");
				if (SamplePoint.distance(pastMeans[i], groups[i].getMeanPoint()) != 0) {
					// 均值发生变化
					reply = true;
				}

			}
		}

		System.out.println(sbuf.toString());

		return reply;

	}

	public static void main(String[] args) throws Exception {
		String file = "data.txt";
		List<SamplePoint> points = Utils.readSamplePoints(file);

		// 设置初始化聚类
		Group[] groups = new Group[2];
		groups[0] = new Group(0);
		groups[0].setMeanPoint(points.get(1));
		groups[1] = new Group(1);
		groups[1].setMeanPoint(points.get(19));

		// 开始聚类
		KMeans km = new KMeans(groups, points);
		km.run();
	}
}
