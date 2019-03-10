package MyCSP.experiment;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import MyCSP.model.STGCS;

public class GetRunningTime {

	public static float getAverage(float[] ft) {

		float r = 0;
		for (int i = 0; i < ft.length; i++) {
//			System.out.println("ft" + i + ft[i]);
			r += ft[i];
		}
		return r / ft.length;
	}

	public static void main(String[] args) {

		List<Float> timeList = new ArrayList<Float>();
		int fq = 10; // ����ÿ����������fq��ȡƽ��
		int sindex = 6;// һ�����ݵ���ʼ����
		int eindex = 75;// һ������β��������
		String dataName = "random";

		for (int i = sindex; i <= eindex; i++) {
			String atmp = dataName;
			if (i < 10) {
				atmp += "0" + i;
			} else {
				atmp += i;
			}
			args[1] = atmp;
			float[] ttmp = new float[fq];
			for (int j = 0; j < fq; j++) {
				new STGCS(ttmp, j).execute(args);
				if (ttmp[j] > 1100) {//����ʱ�䳬��1100��Ϊ��ʱ
					ttmp[j] = -fq;
					break;
				}
//				System.out.println("ttmp:"+ttmp[1]);
			}
			timeList.add(getAverage(ttmp));
		}

		int i = sindex;
		// ��ӡ���
		int failcount = 0;
		float sum = 0.0f;
		for (float pr : timeList) {

			String p = dataName;
			if (i < 10) {
				p += "0" + i;
			} else {
				p += i;
			}
			if (pr > 0) {
				sum += pr;
			} else {
				failcount++;
			}
			DecimalFormat df = new DecimalFormat("0.000");
			System.out.println(p + ":\t" + df.format(pr));
			i++;
		}
		System.out.println("ƽ��ֵ:\t" + sum / (i - failcount));
		System.out.println("ͨ����:\t" + (float)(i - failcount) / i);
//		args[1] += "0" + 5;
//		new STGCS().execute(args);
	}

}
