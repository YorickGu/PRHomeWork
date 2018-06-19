package edu.ict;

/**
 * ����
 * 
 * @author gyj
 * 
 */
public class Group {

	/**
	 * ������ţ����֣�
	 */
	private int number;

	/**
	 * ��ֵ��
	 */
	private SamplePoint meanPoint;

	private float meanDistance;

	/**
	 * ���ݾ�����Ź������
	 * 
	 * @param clusterNumber
	 */
	public Group(int number) {
		this.number = number;
	}

	/**
	 * ���þ�ֵ��
	 * 
	 * @param meanPoint
	 */
	public void setMeanPoint(SamplePoint meanPoint) {
		this.meanPoint = meanPoint;
	}

	/**
	 * ȡ�þ�ֵ��
	 * 
	 * @return
	 */
	public SamplePoint getMeanPoint() {

		return this.meanPoint;

	}

	/**
	 * ȡ�þ������
	 * 
	 * @return
	 */
	public int getNumber() {
		return this.number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public float getMeanDistance() {
		return meanDistance;
	}

	public void setMeanDistance(float meanDistance) {
		this.meanDistance = meanDistance;
	}

	@Override
	public String toString() {
		StringBuffer sbuf = new StringBuffer("groups[" + (number + 1) + "],��ֵ��");
		sbuf.append("(");
		float[] values = meanPoint.getValues();
		for (int i = 0; i < values.length; i++) {
			sbuf.append(values[i]);
			if (i != values.length - 1)
				sbuf.append(", ");
		}
		sbuf.append(")");
		return sbuf.toString();
	}

}
