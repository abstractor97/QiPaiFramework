package com.yaowan.util;

import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class FileUtil {
	
	private FileUtil() {
	}

	/**
	 * 列出指定文件夹的内容 2012-3-14
	 * 
	 * @param dir 指定文件夹的路径
	 * @return 指定文件夹的内容
	 */
	public static String[] ls(String dir) {
		File file = new File(dir);
		if (file.exists()) {
			return file.list();
		}
		return new String[0];
	}

	/**
	 * 写入文件(完全自定义文件格式和写入内容)
	 * 
	 * @param filename 文件名(可包含路径)
	 * @param data 要写入的数据
	 * @param flag 是否追加(true追加，false不追加)
	 * @return boolean
	 */
	public static boolean write(String filename, String data, boolean flag) {
		int i = filename.lastIndexOf('/');
		File file = new File(filename.substring(0, i));
		if (!file.exists()) {
			file.mkdirs();
		}
		try {
			FileWriter fw = new FileWriter(filename, flag);
			fw.write(data);
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 删除某个文件或目录 2012-3-13
	 * 
	 * @param filename
	 *            文件名或目录
	 * @return 0：删除成功，-1：指定文件夹不存在，-2：指定文件不存在
	 */
	public static int delete(String filename) {
		File file = new File(filename);
		if (file.isDirectory()) {
			String[] dir = ls(filename);
			for (int i = 0; i < dir.length; i++) {
				new File(filename + "/" + dir[i]).delete();
				if (file.isDirectory()) {
					delete(filename + "/" + dir[i]);
				}
			}
			if (file.delete()) {
				return 0;
			} else {
				return -1;
			}
		} else if (file.isFile()) {
			if (file.delete()) {
				return 0;
			} else {
				return -2;
			}
		} else {
			return -1;
		}
	}

	/**
	 * 读取文件类容
	 * 
	 * @param filename 文件名(可包含路径)
	 * @return 文件类容
	 */
	public static String read(String filename) {
		try {
			String text = null;
			StringBuilder sb = new StringBuilder();
			BufferedReader input = new BufferedReader(new FileReader(filename));
			while ((text = input.readLine()) != null) {
				sb.append(text);
				sb.append("\n");
			}
			input.close();
			return sb.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * 判断是否为windows系统
	 * @return
	 */
	public static boolean isWindows() {
		return System.getProperty("os.name").indexOf("Windows") != -1;
	}

	/**
	 * 加载一个property文件
	 * @param file property文件的完整路径
	 * @return
	 */
	public final static Properties loadProperties(String file) {
		Properties properties = null;
		try {
			InputStream in = new BufferedInputStream(new FileInputStream(file));
			properties = new Properties();
			properties.load(in);
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return properties;
	}
	
	/**
	 * 获取包下面所有类
	 *
	 * @author tangyangbo 2015年11月27日 
	 * @param pack 包路径
	 * @return
	 */
	public static Set<Class<?>> getClasses(String pack) {

		// 第一个class类的集合
		Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
		// 是否循环迭代
		boolean recursive = true;
		// 获取包的名字 并进行替换
		String packageName = pack;
		String packageDirName = packageName.replace('.', '/');
		// 定义一个枚举的集合 并进行循环来处理这个目录下的things
		Enumeration<URL> dirs;
		try {
			dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
			// 循环迭代下去
			while (dirs.hasMoreElements()) {
				// 获取下一个元素
				URL url = dirs.nextElement();
				// 得到协议的名称
				String protocol = url.getProtocol();
				// 如果是以文件的形式保存在服务器上
				if ("file".equals(protocol)) {
					//System.err.println("file类型的扫描");
					// 获取包的物理路径
					String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
					// 以文件的方式扫描整个包下的文件 并添加到集合中
					findAndAddClassesInPackageByFile(packageName, filePath, recursive, classes);
				} else if ("jar".equals(protocol)) {
					// 如果是jar包文件
					// 定义一个JarFile
					//System.err.println("jar类型的扫描");
					JarFile jar;
					try {
						// 获取jar
						jar = ((JarURLConnection) url.openConnection()).getJarFile();
						// 从此jar包 得到一个枚举类
						Enumeration<JarEntry> entries = jar.entries();
						// 同样的进行循环迭代
						while (entries.hasMoreElements()) {
							// 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
							JarEntry entry = entries.nextElement();
							String name = entry.getName();
							// 如果是以/开头的
							if (name.charAt(0) == '/') {
								// 获取后面的字符串
								name = name.substring(1);
							}
							// 如果前半部分和定义的包名相同
							if (name.startsWith(packageDirName)) {
								int idx = name.lastIndexOf('/');
								// 如果以"/"结尾 是一个包
								if (idx != -1) {
									// 获取包名 把"/"替换成"."
									packageName = name.substring(0, idx).replace('/', '.');
								}
								// 如果可以迭代下去 并且是一个包
								if ((idx != -1) || recursive) {
									// 如果是一个.class文件 而且不是目录
									if (name.endsWith(".class") && !entry.isDirectory()) {
										// 去掉后面的".class" 获取真正的类名
										String className = name.substring(packageName.length() + 1, name.length() - 6);
										try {
											// 添加到classes
											classes.add(Class.forName(packageName + '.' + className));
										} catch (ClassNotFoundException e) {
											// log
											// .error("添加用户自定义视图类错误
											// 找不到此类的.class文件");
											e.printStackTrace();
										}
									}
								}
							}
						}
					} catch (IOException e) {
						// log.error("在扫描用户定义视图时从jar包获取文件出错");
						e.printStackTrace();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return classes;
	}

	public static void findAndAddClassesInPackageByFile(String packageName, String packagePath, final boolean recursive,
			Set<Class<?>> classes) {
		// 获取此包的目录 建立一个File
		File dir = new File(packagePath);
		// 如果不存在或者 也不是目录就直接返回
		if (!dir.exists() || !dir.isDirectory()) {
			// log.warn("用户定义包名 " + packageName + " 下没有任何文件");
			return;
		}
		// 如果存在 就获取包下的所有文件 包括目录
		File[] dirfiles = dir.listFiles(new FileFilter() {
			// 自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
			public boolean accept(File file) {
				return (recursive && file.isDirectory()) || (file.getName().endsWith(".class"));
			}
		});
		// 循环所有文件
		for (File file : dirfiles) {
			// 如果是目录 则继续扫描
			if (file.isDirectory()) {
				findAndAddClassesInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(), recursive,
						classes);
			} else {
				// 如果是java类文件 去掉后面的.class 只留下类名
				String className = file.getName().substring(0, file.getName().length() - 6);
				try {
					// 添加到集合中去
					// classes.add(Class.forName(packageName + '.' +
					// className));
					// 经过回复同学的提醒，这里用forName有一些不好，会触发static方法，没有使用classLoader的load干净
					classes.add(
							Thread.currentThread().getContextClassLoader().loadClass(packageName + '.' + className));
				} catch (ClassNotFoundException e) {
					// log.error("添加用户自定义视图类错误 找不到此类的.class文件");
					e.printStackTrace();
				}
			}
		}
	}
}
