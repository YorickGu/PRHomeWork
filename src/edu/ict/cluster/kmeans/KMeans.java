package edu.ict.cluster.kmeans;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

import edu.ict.Group;
import edu.ict.SamplePoint;
import edu.ict.Utils;

/**
 * K��ֵ����������
 * 
 * @author gyj
 * 
 */
public class KMeans {

	/**
	 * ������
	 */
	private int k;

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

	public KMeans(Group[] groups, List<SamplePoint> points) {
		this.k = groups.length;
		this.groups = groups;
		this.points = points;

		this.dimension = points.get(0).getDimension();

	}

	/**
	 * ����K��ֵ�����㷨
	 */
	public void run() {
		// ��������
		int nIters = 1;
		do {
			System.out.println("--------��������ţ�" + nIters + "-------");
			Iterator<SamplePoint> iterPoints = this.points.iterator();
			while (iterPoints.hasNext()) {
				SamplePoint point = iterPoints.next();

				int nearest = point.getGroupNumber();
				float minDistance = SamplePoint.distance(point, this.groups[nearest]
						.getMeanPoint());

				// Ѱ���������ľ���
				for (int i = 0; i < this.k; i++) {
					float distance = SamplePoint
							.distance(point, groups[i].getMeanPoint());
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

			StringBuffer sbuf = new StringBuffer("���ε��������");
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
	 * ���µ�ǰ������ľ�ֵ����
	 * 
	 * @return ��û�и����򷵻�false���и����򷵻�true
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

		StringBuffer sbuf = new StringBuffer("���¾���ľ�ֵ��Ϊ��");

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
					// ��ֵ�����仯
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

		// ���ó�ʼ������
		Group[] groups = new Group[2];
		groups[0] = new Group(0);
		groups[0].setMeanPoint(points.get(1));
		groups[1] = new Group(1);
		groups[1].setMeanPoint(points.get(19));

		// ��ʼ����
		KMeans km = new KMeans(groups, points);
		km.run();
	}
}
