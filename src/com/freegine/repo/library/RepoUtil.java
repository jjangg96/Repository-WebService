package com.freegine.repo.library;

public class RepoUtil {

	
	//C 스타일의 Unsigned 형식으로 변경 
	public static class UnsignedUtil
	{
		
		public static int byte2uchar(byte b)
		{
			return (int)(b&0xff);
		}
		
		public static byte uchar2byte(int c)
		{
			return (byte)(c & 0xff);
		}
		
		public static int short2ushort(short s)
		{
			return (int)(s & 0xffff);
		}
		
		public static short ushort2short(int s)
		{
			return (short)(s & 0xffff);
		}
		
		public static long int2uint(int i)
		{
			return (long)(i & 0xffffffffL);
		}
		
		public static int uint2int(long i)
		{
			return (int)(i & 0xffffffffL);
		}
	}
}