package com.yaowan.util;

import java.util.Calendar;
import java.util.concurrent.ThreadLocalRandom;

import com.yaowan.constant.GameType;
import com.yaowan.framework.util.TimeUtil;

public class RandomPlayerCount {
	/*
	 * private static int amLower = 60;
	 * 
	 * private static int mornLower = 90;
	 * 
	 * private static int midLower = 130;
	 * 
	 * private static int afterLower = 110;
	 * 
	 * private static int nightLower = 160;
	 */

	private static final int menji = 15;

	private static final int majiang = 75;

	private static final int doudizhu = 90;

	private RandomPlayerCount() {
	}

/*	public static void main(String[] args) {
		for (int j = 1; j < 4; j++) {
			System.out.println("=====================" + j
					+ "===================================");
			for (int i = 0; i < 24; i++) {
				System.out.println(i+"=" + roomCount(j, i));
			}
		}

	}*/

	public static int roomCount(int gameType) {

		Calendar calendar = Calendar.getInstance();
		int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
		int time = TimeUtil.time();
		int c = 10;
		int result = 0;
		ThreadLocalRandom random = ThreadLocalRandom.current();
		switch (gameType) {
		case GameType.DOUDIZHU:
			c = random.nextInt(1, 20);
			result = getCount(hourOfDay, time, c, doudizhu);
			break;
		case GameType.MENJI:
			c = random.nextInt(1, 20);
			result = getCount(hourOfDay, time, c, menji);
			break;
		case GameType.MAJIANG:
		case GameType.ZXMAJIANG:
		case GameType.CDMAJIANG:
			c = random.nextInt(1, 20);
			result = getCount(hourOfDay, time, c, majiang);
			break;
		case GameType.DEZHOU:
			c = random.nextInt(1, 20);
			result = getCount(hourOfDay, time, c, majiang);
			break;
		}
		return result;
	}

	private static int getCount(int hourOfDay, int time, int c,
			int gameTypeCount) {
		int amLower = 30;

		int mornLower = 50;

		int midLower = 60;

		int afterLower = 50;

		int nightLower = 80;
		int count = 10;
		amLower -= c;

		mornLower -= c;

		midLower -= c;

		afterLower -= c;
		switch (hourOfDay) {
		case 0:
			count = getSinValue(time, amLower, 1.5, c * 2);// 50~65
			count += (int) ( gameTypeCount * 0.75);
			break;
		case 1:
			count = getSinValue(time, amLower, 1, c * 2);// 50~60
			count += (int) ( gameTypeCount * 0.35);
			break;
		case 2:
			count = getSinValue(time, amLower, 1, c * 2);// 50~60
			count += (int) ( gameTypeCount * 0.15);
			break;
		case 3:
			count = getSinValue(time, amLower, 1, c * 2);// 50~60
			count += (int) ( gameTypeCount * 0.1);
			break;
		case 4:
			count = getSinValue(time, amLower, 1, c * 2);// 50~60
			count += (int) (gameTypeCount * 0.1);
			break;
		case 5:
			count = getSinValue(time, amLower, 1, c * 2);// 50~60
			count += (int) ( gameTypeCount * 0.1);
			break;
		case 6:
			count = getSinValue(time, amLower, 1.5, c * 2);// 50~65
			count += (int) (gameTypeCount * 0.25);
			break;
		case 7:
			count = getSinValue(time, amLower, 1.5, c * 2);// 50~65
			count += (int) ( gameTypeCount * 0.4);
			break;
		case 8:
			count = getSinValue(time, mornLower, 3, c * 3);// 80~110
			count += (int) ( gameTypeCount * 0.7);
			break;
		case 9:
			count = getSinValue(time, mornLower, 4, c * 3);// 80~120
			count += (int) (gameTypeCount * 0.88);
			break;
		case 10:
			count = getSinValue(time, mornLower, 3, c * 3);// 80~110
			count += (int) (gameTypeCount * 0.95);
			break;
		case 11:
			count = getSinValue(time, mornLower, 3, c * 3);// 80~110
			count += (int) ( gameTypeCount * 0.90);
			break;
		case 12:
			count = getSinValue(time, midLower, 2, c * 3);// 120~140
			count += (int) ( gameTypeCount * 0.98);
			break;
		case 13:
			count = getSinValue(time, afterLower, 1, c * 3);// 100~110
			count += (int) (gameTypeCount * 0.95);
			break;
		case 14:
			count = getSinValue(time, afterLower, 1, c * 3);// 100~110
			count += (int) ( gameTypeCount * 0.80);
			break;
		case 15:
			count = getSinValue(time, afterLower, 1, c * 3);// 100~110
			count += (int) ( gameTypeCount * 0.80);
			break;
		case 16:
			count = getSinValue(time, afterLower, 1, c * 3);// 100~110
			count += (int) (gameTypeCount * 0.85);
			break;
		case 17:
			count = getSinValue(time, afterLower, 2, c * 3);// 100~120
			count += (int) (gameTypeCount * 0.75);
			break;
		case 18:
			count = getSinValue(time, afterLower, 3, c * 3);// 100~130
			count += (int) ( gameTypeCount * 0.85);
			break;
		case 19:
			count = getSinValue(time, nightLower, 2, c * 3);// 150~170
			count += (int) (gameTypeCount * 0.98);
			break;

		case 20:
			count = getSinValue(time, nightLower, 3, c * 3);// 150~180
			count += (int) ( gameTypeCount * 0.95);
			break;

		case 21:
			count = getSinValue(time, nightLower, 4, c * 3);// 150~190
			count += (int) (gameTypeCount * 0.98);
			break;
		case 22:
			count = getSinValue(time, nightLower, 3, c * 3);// 150~180
			count += (int) ( gameTypeCount * 0.95);
			break;
		case 23:
			count = getSinValue(time, midLower, 2, c * 3);// 120~140
			count+= (int) ( gameTypeCount * 0.75);
			break;
		}
		return count;
	}

	/**
	 * 公式为：y = Ksin(aX+c)+b；
	 * 
	 * @author G_T_C
	 * @param time
	 * @param lowerVale
	 * @param k
	 * @param a
	 * @param c
	 * @return
	 */
	private static int getSinValue(int time, int lowerVale, double k, int c) {
		Double x = Math.toRadians(time);
		Double value = Math.abs(10 * k * Math.sin(c * x + c));
		int result = value.intValue();
		return result + lowerVale;
	}
}
