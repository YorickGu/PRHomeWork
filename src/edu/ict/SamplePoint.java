package edu.ict;

/**
 * ��������
 * 
 * @author ysh
 * 
 */
public class SamplePoint {

	private int index;

	/**
	 * ģʽ��������������
	 */
	private float[] values;

	/**
	 * �������Ĵ���
	 */
	private int groupNumber;

	public SamplePoint(int index, float[] values) {
		this.index = index;
		this.values = values;
	}

	public int getGroupNumber() {
		return groupNumber;
	}

	public void setGroupNumber(int groupNumber) {
		this.groupNumber = groupNumber;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public float[] getValues() {
		return values;
	}

	public void setValues(float[] values) {
		this.values = values;
	}

	public int getDimension() {
		return values.length;
	}

	@Override
	public String toString() {
		StringBuffer sbuf = new StringBuffer();
		if (index > 0)
			sbuf.append("x" + index + " (");
		else
			sbuf.append("��ֵ�� (");
		for (int i = 0; i < values.length; i++) {
			sbuf.append(values[i]);
			if (i != values.length - 1)
				sbuf.append(", ");
		}
		sbuf.append(")");
		return sbuf.toString();
	}

	/**
	 * ����ģʽ�������ŷʽ����
	 * 
	 * @param dp1
	 *            ģʽ����1
	 * @param dp2
	 *            ģʽ����2
	 * @return ��ģʽ�������ŷʽ����
	 */
	public static float distance(SamplePoint dp1, SamplePoint dp2) {
		float result = 0;
		float[] values1 = dp1.getValues();
		float[] values2 = dp2.getValues();
		for (int i = 0; i < values1.length; i++) {
			float dis = values1[i] - values2[i];
			result += dis * dis;
		}
		return (float) Math.sqrt(result);
	}

}
