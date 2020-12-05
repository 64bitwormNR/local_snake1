package snake_server1;

import java.util.*;
import javax.swing.*;
import java.awt.Font;
import java.io.*;
import java.net.*;

class point
{
	int x,y;
	point(int a,int b)
	{
		x=a;
		y=b;
	}
	@Override
	public boolean equals(Object a)
	{
		return x==((point)a).x && y==((point)a).y;
	}
	public void add1(point a)
	{
		x+=a.x;
		y+=a.y;
	}
	public point clone1()
	{
		return new point(x,y);
	}
}

class player
{
	String ip1;
	String name1;
	char dir1;
	int score1;
	int color1;
	ArrayList<point> parts1=new ArrayList<>();
	long acttime1;
}

class game extends Thread
{
	ArrayList<player> players1=new ArrayList<player>(); 
	ArrayList<point> apples1=new ArrayList<>();
	char[][] map1=new char[32][32];
	int mapid1;
	int speed1=0;
	int[] speeds1={300,150,450,600,750};
	int[] aplsc1={1,2,3,5,7,10,15,20,30,50};
	boolean friendly1=false;
	boolean corpse2block1=false;
	boolean start1=false;
	int apples_count1=1;
	long check_offline1=System.currentTimeMillis();
	volatile String ranking_table1="You need to play first game :)";
	volatile String cmap1="loading";
	
	@SuppressWarnings("deprecation")
	@Override
	public void run()
	{
		while(true)
		{
			try
			{
				Thread.sleep(Math.max(100,speeds1[speed1]));
				if(!start1)
					continue;
				char[][] outmap1=new char[map1.length][];
				for(int i=0;i<map1.length;i++)
					outmap1[i]=map1[i].clone();
				for(int i=0;i<players1.size();i++)
				{
					player cp1=players1.get(i);
					if(cp1.dir1=='-')
						continue;
					point add1=new point(0,0);
					if(cp1.dir1=='a')
						add1=new point(-1,0);
					else if(cp1.dir1=='s')
						add1=new point(0,1);
					else if(cp1.dir1=='w')
						add1=new point(0,-1);
					else if(cp1.dir1=='d')
						add1=new point(1,0);
					point lp1=cp1.parts1.get(cp1.parts1.size()-1).clone1();
					lp1.add1(add1);
					lp1.x=Main.mod1(lp1.x,32);
					lp1.y=Main.mod1(lp1.y,32);
					int apl1=apples1.indexOf(lp1);
					if(apl1!=-1)
					{
						point ap1=apples1.get(apl1);
						cp1.score1+=(ap1.x%2==0 && ap1.y%2==0) ? 3:1;
						apples1.remove(apl1);
					}
					else
						cp1.parts1.remove(0);
					cp1.parts1.add(lp1);
				}
				for(int i=0;i<players1.size();i++)
				{
					player cp1=players1.get(i);
					point head1=cp1.parts1.get(cp1.parts1.size()-1);
					boolean alive1=true;
					if(!friendly1)
					for(int i1=0;i1<players1.size();i1++)
					if(i!=i1 && players1.get(i1).parts1.indexOf(head1)!=-1)
					{
						alive1=false;
						break;
					}
					if(outmap1[head1.y][head1.x]=='1' || cp1.parts1.indexOf(head1)!=cp1.parts1.size()-1)
						alive1=false;
					if(!alive1)
					{
						cp1.dir1='-';
						if(corpse2block1)
							for(int i1=0;i1<cp1.parts1.size()-1;i1++)
								map1[cp1.parts1.get(i1).y][cp1.parts1.get(i1).x]='1';
					}
				}
				int alive_players1=0;
				for(int i=0;i<players1.size();i++)
					if(players1.get(i).dir1!='-')
					{
						for(point prt1: players1.get(i).parts1)
							outmap1[prt1.y][prt1.x]=(char)('a'+i);
						alive_players1++;
					}
				if(apples1.size()<aplsc1[apples_count1])
					apples1.add(free1(outmap1));
				for(point ap1: apples1)
					outmap1[ap1.y][ap1.x]=(ap1.x%2==0 && ap1.y%2==0) ? '3':'2';
				cmap1="";
				for(char[] line1: outmap1)
					cmap1+=new String(line1);
				if(alive_players1==0)
				{
					cmap1="ranking";
					start1=false;
					ranking_table1="<table bgcolor='gray' style='border: 1px solid black'>";
					ArrayList<String> names1=new ArrayList<>();
					ArrayList<Integer> score1=new ArrayList<>();
					for(player cpl1: players1)
					{
						names1.add(cpl1.name1);
						score1.add(cpl1.score1);
					}
					int[] idx1=new int[names1.size()];
					for(int i=0;i<idx1.length;i++)
						idx1[i]=i;
					for(int i=1;i<idx1.length;i++)
					for(int i1=i;i1>0;i1--)
						if(score1.get(idx1[i1])>score1.get(idx1[i1-1]))
						{
							int t1=idx1[i1];
							idx1[i1]=idx1[i1-1];
							idx1[i1-1]=t1;
						}
						else
							break;
					ranking_table1+="<tr><th style='border: 1px solid black'>Rank</th><th style='border: 1px solid black'>Username</th><th style='border: 1px solid black'>Score</th></tr>";
					for(int i=0;i<idx1.length;i++)
						ranking_table1+="<tr><th style='border: 1px solid black'>"+(i+1)+"</th><th style='border: 1px solid black'>"+names1.get(idx1[i])+"</th><th style='border: 1px solid black'>"+score1.get(idx1[i])+"</th></tr>";
					Date dt1=new Date();
					ranking_table1+="</table>"+"<p>Last game time: "+(dt1.getHours()+":"+dt1.getMinutes())+"</p>";
				}
			}
			catch(Exception e)
			{}
		}
	}
	
	public point free1(char[][] a)
	{
		int totalfree1=0;
		for(int y=0;y<32;y++)
		for(int x=0;x<32;x++)
		if(a[y][x]=='0')
			totalfree1++;
		totalfree1=(int)(Math.random()*totalfree1);
		for(int y=0;y<32;y++)
		for(int x=0;x<32;x++)
		if(a[y][x]=='0')
			if(totalfree1==0)
				return new point(x,y);
			else
				totalfree1--;
		return new point(0,0);
	}
	
	public int findbyip1(String a)
	{
		for(int i=0;i<players1.size();i++)
			if(a.equals(players1.get(i).ip1))
				return i;
		return -1;
	}
}

class sckthr1 extends Thread
{
	Socket sck1;
	volatile static int total1=0;
	static final String head1="HTTP/1.1 200 OK\r\nServer: mai-snake\r\nConnection: close\r\n\r\n";
	
	sckthr1(Socket a)
	{
		sck1=a;
	}
	
	@Override
	public void run()
	{
		total1++;
		try
		{
			String ip1=sck1.getInetAddress().toString().substring(1);
			long stime1=System.currentTimeMillis();
			//System.out.println(ip1);
			InputStream is1=sck1.getInputStream();
			OutputStream os1=sck1.getOutputStream();
			ByteArrayOutputStream bos1=new ByteArrayOutputStream();
			while(is1.available()==0)
			{
				Thread.sleep(10);
				if(System.currentTimeMillis()>stime1+1000)
				{
					sck1.close();
					return;
				}
			}
			byte[] buf1=new byte[1024];
			while(is1.available()>0 && bos1.size()<10240)
			{
				int r1=is1.read(buf1);
				bos1.write(buf1,0,r1);
				Thread.sleep(15);
			}
			//System.out.println(new String(bos1.toByteArray()));
			String[] inp1=new String(bos1.toByteArray()).split("\r\n")[0].split(" ")[1].split("/");
			int pid1=Main.mg1.findbyip1(ip1);
			if(System.currentTimeMillis()-Main.mg1.check_offline1>4000)
			{
				for(int i=Main.mg1.players1.size()-1;i>-1;i--)
					if(System.currentTimeMillis()-Main.mg1.players1.get(i).acttime1>5000)
					{
						Main.mg1.players1.remove(i);
						if(i==pid1)
							pid1=-1;
					}
					else if(i==pid1)
						Main.mg1.players1.get(pid1).acttime1=System.currentTimeMillis();
			}
			if(inp1.length==0)
				os1.write((head1+Main.mainjs1+"\r\n\r\n").getBytes());
			else switch(inp1[1])
			{
				case "status":
				{
					if(pid1==-1)
						os1.write((head1+"login").getBytes());
					else
						os1.write((head1+"lobby").getBytes());
					break;
				}
				case "login":
				{
					if(pid1==-1 && Main.mg1.players1.size()<Main.maxplayers1(Main.maps1.get(Main.mg1.mapid1)) && !Main.mg1.start1)
					{
						if(inp1[2].length()<3 || inp1[2].length()>15 || !Main.isnickname1(inp1[2]))
						{
							os1.write((head1+"wrong").getBytes());
							break;
						}
						player np1=new player();
						np1.ip1=ip1;
						np1.name1=inp1[2];
						boolean freename1=true;
						for(player ncp1: Main.mg1.players1)
							if(ncp1.name1.equals(np1.name1))
							{
								freename1=false;
								break;
							}
						if(!freename1)
						{
							os1.write((head1+"wrong").getBytes());
							break;
						}
						np1.acttime1=System.currentTimeMillis();
						Main.mg1.players1.add(np1);
						os1.write((head1+"ok").getBytes());
					}
					else
						os1.write((head1+"spectate").getBytes());
					break;
				}
				case "lobby-get":
				{
					//if(pid1==-1)
						//break;
					String players1="";
					for(player pn: Main.mg1.players1)
						players1+=", "+pn.name1;
					if(players1.length()>0)
						players1=players1.substring(1);
					os1.write((head1+Main.mg1.mapid1+";"+Main.mg1.speed1+";"+(Main.mg1.friendly1 ? "1":"0")+";"+Main.mg1.apples_count1+";"+(Main.mg1.corpse2block1 ? "1":"0")+";"+players1+";"+(Main.mg1.start1 ? "1":"0")).getBytes());
					break;
				}
				case "lobby-set":
				{
					if(pid1==-1 || Main.mg1.start1)
						break;
					if(inp1[2].equals("start") && !Main.mg1.start1)
					{
						Main.mg1.start1=true;
						Main.mg1.apples1.clear();
						for(int i=0;i<Main.mg1.players1.size();i++)
						{
							Main.mg1.players1.get(i).color1=i;
							Main.mg1.players1.get(i).dir1='d';
							Main.mg1.players1.get(i).parts1.clear();
							Main.mg1.players1.get(i).score1=0;
						}
						int x=0;
						int y=0;
						String map1=Main.maps1.get(Main.mg1.mapid1);
						for(int i=0;i<map1.length();i++)
						{
							char cc1=map1.charAt(i);
							if(cc1=='\r')
								continue;
							if(cc1=='\n')
							{
								y++;
								x=0;
								continue;
							}
							if(cc1>='a' && cc1<='z')
							{
								int pp1=(int)(cc1-'a');
								if(pp1<Main.mg1.players1.size())
								{
									Main.mg1.players1.get(pp1).parts1.add(new point(x,y));
								}
								cc1='0';
							}
							Main.mg1.map1[y][x]=cc1;
							x++;
						}
						os1.write((head1+"ok").getBytes());
						break;
					}
					String[] prts1=inp1[2].split(";");
					Main.mg1.mapid1=Integer.parseInt(prts1[0]);
					Main.mg1.speed1=Math.max(0,Math.min(Main.mg1.speeds1.length-1,Integer.parseInt(prts1[1])));
					Main.mg1.friendly1=prts1[2].equals("1");
					Main.mg1.apples_count1=Integer.parseInt(prts1[3]);
					Main.mg1.corpse2block1=prts1[4].equals("1");
					os1.write((head1+"ok").getBytes());
					break;
				}
				case "game":
				{
					if(pid1!=-1)
					{
						char dr1=inp1[2].charAt(0);
						if(dr1!='a' && dr1!='w' && dr1!='s' && dr1!='d')
							dr1='-';
						char cur1=Main.mg1.players1.get(pid1).dir1;
						if(cur1!='-' && !(cur1=='a' && dr1=='d') && !(cur1=='w' && dr1=='s') && !(cur1=='s' && dr1=='w') && !(cur1=='d' && dr1=='a'))
							Main.mg1.players1.get(pid1).dir1=inp1[2].charAt(0);
					}
					os1.write((head1+Main.mg1.cmap1).getBytes());
					break;
				}
				case "ranking":
				{
					os1.write((head1+Main.mg1.ranking_table1).getBytes());
					break;
				}
				default:
					os1.write("unsupported".getBytes());
			}
			os1.flush();
			sck1.close();
		}
		catch(Exception e)
		{
			if(Main.debug1)
				e.printStackTrace();
		}
		total1--;
	}
}

public class Main
{
	static ServerSocket serv1;
	static String mainjs1;
	static String path1;
	static ArrayList<String> maps1=new ArrayList<String>();
	static game mg1;
	static boolean debug1=true;
	
	public static void main(String[] args)
	{
		JFrame frame1 = new JFrame("Local Snake Server");
		frame1.setResizable(false);
		frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame1.setSize(350, 100);
		frame1.setLocation(100, 100);
		JLabel jl1=new JLabel("Loading...");
		jl1.setFont(new Font(jl1.getFont().getName(),0,16));
		frame1.getContentPane().add(jl1);
		frame1.setVisible(true);
		jl1.setText("  Server Started at: http://"+myip1()+":777");
		path1=System.getProperty("user.dir")+"\\snake_server1\\";
		path1="/"; //reading from resources
		mainjs1=readfile1(path1+"index.html");
		for(int i=1;;i++)
		{
			String cmap1=readfile1(path1+"map"+i+".txt");
			if(cmap1==null)
				break;
			maps1.add(cmap1);
		}
		mg1=new game();
		mg1.start();
		try
		{
			serv1=new ServerSocket(777);
			while(true)
			{
				if(sckthr1.total1<26)
					new sckthr1(serv1.accept()).start();
				else
					Thread.sleep(100);
			}
		}
		catch(Exception e)
		{
			if(Main.debug1)
				e.printStackTrace();
		}
	}
	
	static int maxplayers1(String a)
	{
		int plc1=0;
		for(int x1=0;x1<a.length();x1++)
		{
			char cc1=a.charAt(x1);
			if(cc1<'a' || cc1>'z')
				continue;
			plc1=Math.max(plc1,(int)(cc1-'a')+1);
		}
		return plc1;
	}
	
	static boolean isnickname1(String a)
	{
		for(char c: a.toCharArray())
			if(!((c>='a' && c<='z') || (c>='A' && c<='Z') || (c>='0' && c<='9') || c==' ' || c=='-'))
				return false;
		return true;
	}
	
	static int mod1(int a,int b)
	{
		return (a%b+b)%b;
	}
	
	static String myip1()
	{
		try
		{
			return InetAddress.getLocalHost().getHostAddress();
		}
		catch(Exception e)
		{
			return null;
		}
	}
	
	static String rands1(String a,int b)
	{
		String ret1="";
		for(;b>0;b--)
			ret1+=a.charAt((int)(Math.random()*a.length()));
		return ret1;
	}
	
	static String readfile1(String a)
	{
		try
		{
			InputStream fis1=Main.class.getResourceAsStream(a);
			byte[] in1=new byte[fis1.available()];
			fis1.read(in1);
			fis1.close();
			return new String(in1);
		}
		catch(Exception e)
		{
			return null;
		}
	}

}
