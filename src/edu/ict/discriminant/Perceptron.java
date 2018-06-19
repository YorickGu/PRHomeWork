package edu.ict.discriminant;

import java.util.List;

import org.apache.commons.lang.ArrayUtils;

import edu.ict.SamplePoint;
import edu.ict.Utils;

/**
 * ��֪���㷨ʵ����
 * 
 * @author ysh
 * 
 */
public class Perceptron {
	/**
	 * ��������������
	 */
	private List<SamplePoint> points;

	/**
	 * Ȩ����
	 */
	private float[] w;

	/**
	 * ��������ϵ��
	 */
	private float coef;

	/**
	 * ��֪���㷨ʵ���๹�캯��
	 * 
	 * @param points
	 *            ������
	 * @param w
	 *            ��ʼȨ����
	 * @param coef
	 *            ��������ϵ��
	 */
	public Perceptron(List<SamplePoint> points, float[] w, float coef) {
		super();
		this.points = points;
		this.w = w;
		this.coef = coef;

		for (SamplePoint point : points) {
			if (point.getGroupNumber() != 1) {
				// �����ڵ�һ�������ģʽ����������-1
				float[] values = point.getValues();
				for (int i = 0; i < values.length; i++) {
					values[i] = -values[i];
				}
			}
		}
	}

	/**
	 * ���и�֪���㷨
	 */
	public void run() {

		boolean ok = false;

		// ����������Ȩ������w������������
		int nIters = 1, count = 1;

		while (!ok) {
			ok = true;

			System.out.println("--------��������ţ�" + nIters + "-------");

			for (SamplePoint point : points) {
				if (!discriminant(point)) {
					// point�����
					ok = false;
					float[] values = point.getValues();

					System.out.println(point + " �����");
					StringBuffer sbuf = new StringBuffer("����Ȩ���� w(" + count + ")="
							+ ArrayUtils.toString(w) + " -> ");

					for (int i = 0; i < w.length; i++) {
						w[i] += coef * values[i];
					}
					count++;

					sbuf.append("w(" + count + ")=" + ArrayUtils.toString(w));
					System.out.println(sbuf.toString());
				}
			}
			nIters++;
		}

		System.out.println("\n����wΪ��" + ArrayUtils.toString(w));
	}

	private boolean discriminant(SamplePoint point) {
		float[] values = point.getValues();
		float discrnt_value = 0;
		for (int i = 0; i < values.length; i++) {
			discrnt_value += w[i] * values[i];
		}
		return discrnt_value > 0;
	}

	public static void main(String[] args) throws Exception {
		String file = "perceptron_data_1.txt";
		List<SamplePoint> points = Utils.readLabeledSamplePoints(file);
		float[] w = new float[] { 0, 0, 0, 0 };
		Perceptron perceptron = new Perceptron(points, w, 1.0f);
		perceptron.run();
	}
}
