/**
 * 
 */
package com.yaowan.simulator;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.google.protobuf.GeneratedMessageLite;
import com.yaowan.framework.core.handler.ProtobufCenter;
import com.yaowan.protobuf.game.GFriend.GFamilarFriend;
import com.yaowan.protobuf.game.GFriend.GMsg_11004006;

/**
 * @author huangyuyuan
 *
 */
public class SimulatorGamePanel extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private SimulatorNetty netty = SimulatorNetty.getInstance();
	
	private static String openId = "hyy12345";
//	private static String host = "113.107.95.248:11021";
	private static String host = "127.0.0.1:11401";
	
	private JTextField openIdInput;
	private JComboBox<String> protocolCombo;
	private JTextField hostInput;
	private String selectedStr = null;
	private JComboBox<String> serverCombo;
	private JTextField keyInput;
	
	private Map<Integer, Class<? extends GeneratedMessageLite>> protocolMap;
	
	private String connectType = "GameServer";
	
	public SimulatorGamePanel() {
		
		this.setPreferredSize(new Dimension(280, 180));
		
		constraints.weighty = 8;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		GridBagLayout gridLayout = new GridBagLayout();
		this.setLayout(gridLayout);
		
		JLabel serverLabel = new JLabel("server：", SwingConstants.RIGHT);
		serverCombo = new JComboBox<String>();
		serverCombo.setPreferredSize(new Dimension(100, 20));
		serverCombo.setMaximumRowCount(3);
		add(serverLabel, 0, 1, 1, 1);
		add(serverCombo, 1, 1, 2, 1);
		
		JLabel keyLabel = new JLabel("MD5Key：", SwingConstants.RIGHT);
		keyInput = new JTextField("^_^dfh3:start@2015-09-24!", 14);
		add(keyLabel, 0, 2, 1, 1);
		add(keyInput, 1, 2, 1, 1);
		
		
		JLabel openIdLabel = new JLabel("openId：", SwingConstants.RIGHT);
		openIdInput = new JTextField(openId, 14);
		
		JButton createButton = new JButton("创号");
		createButton.addActionListener(new CreateActionListener());
		
		add(openIdLabel, 	0, 3, 1, 1);
		add(openIdInput, 	1, 3, 1, 1);
		add(createButton, 	2, 3, 1, 1);
		
		JLabel hostLabel = new JLabel("host：", SwingConstants.RIGHT);
		hostInput = new JTextField(host, 14);
		JButton connectButton = new JButton("连接");
		connectButton.addActionListener(new ConnectActionListener());
		
		add(hostLabel, 		0, 4, 1, 1);
		add(hostInput, 		1, 4, 1, 1);
		add(connectButton,  2, 4, 1, 1);
		
		JLabel plabel = new JLabel("protocol：", SwingConstants.RIGHT);
		protocolCombo = new JComboBox<String>();
		protocolCombo.setPreferredSize(new Dimension(215, 20));
		protocolCombo.setMaximumRowCount(25);
		
		add(plabel,        0, 5, 1, 1);
		add(protocolCombo, 1, 5, 2, 1);
		
		JLabel title = new JLabel("参数：", SwingConstants.RIGHT);
		JButton send = new JButton("发送");
		
		send.addActionListener(new SendButtonActionListener());
		JPanel emptyPanel = new JPanel();
		emptyPanel.setPreferredSize(new Dimension(157, 0));
		
		add(title, 		0, 6, 1, 1);
		add(emptyPanel, 1, 6, 1, 1);
		add(send, 		2, 6, 1, 1);
		
		
		listProtocol();
		listServer();
	}
	
	public class CreateActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			String text = hostInput.getText();
			if (text == null || text.isEmpty()) {
				return;
			}

			String[] arr = text.split(":");
			
			netty.connect(arr[0], Integer.parseInt(arr[1]));
			
			String openId = openIdInput.getText();
			if (openId.isEmpty()) {
				return;
			}
			String md5Key = keyInput.getText();
			if (md5Key.isEmpty()) {
				return;
			}
			netty.create(openId, md5Key);
		}
	}
	
	public class ConnectActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			String text = hostInput.getText();
			if (text == null || text.isEmpty()) {
				return;
			}

			String[] arr = text.split(":");
			
			netty.connect(arr[0], Integer.parseInt(arr[1]));
			
			String openId = openIdInput.getText();
			if (openId.isEmpty()) {
				return;
			}
			String md5Key = keyInput.getText();
			if (md5Key.isEmpty()) {
				return;
			}
			if("GameServer".equals(connectType)) {
				netty.login(openId, md5Key);
			} else {
				netty.connectCenter();
			}
		}
	}
	
	private GridBagConstraints constraints = new GridBagConstraints();
	
	public void add(Component c, int x, int y, int w, int h) {
		constraints.gridx = x;
		constraints.gridy = y;
		constraints.gridwidth = w;
		constraints.gridheight = h;
		this.add(c, constraints);
	}
	
	public void listProtocol() {
		ProtobufCenter.init();
		
		protocolMap = ProtobufCenter.getProtocolClzMap();
		
		List<Integer> list = new ArrayList<Integer>();
		for(Integer protocol : protocolMap.keySet()) {
			list.add(protocol);
		}
		Collections.sort(list);
		
		for(Integer protocol : list) {
			if (protocol / 1000000 % 2 != 1) {
				continue;
			}
			Class<?> clz = protocolMap.get(protocol);
			if(clz == null) {
				continue;
			}
			protocolCombo.addItem(protocol + "-" + clz.getSimpleName());
		}
		protocolCombo.addItemListener(new ComboBoxListener());
	}
	
	private class ComboBoxListener implements ItemListener {
		@Override
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() != ItemEvent.SELECTED) {
				return;
			}
			selectedStr = e.getItem().toString();
			fillUpInput();
		}
	}
	
	public void fillUpInput() {
		String[] strs = selectedStr.split("-");
		int protocol = Integer.parseInt(strs[0]);
		Class<? extends GeneratedMessageLite> selectClz = protocolMap.get(protocol);
		
		Class<?> buildClass = null;
		for(Class<?> cls : selectClz.getDeclaredClasses()) {
			if("Builder".equals(cls.getSimpleName())) {
				buildClass = cls;
				break;
			}
		}
		String text = "";
		for(Field field : buildClass.getDeclaredFields()) {
			if("bitField0_".equals(field.getName())) {
				continue;
			}
			text += "\n" + field.getName().replaceAll("_", "") + "=";
		}
		
		SimulatorClient.getInstance().setInputText(text);
	}
	
	public void listServer() {
		
		serverCombo.addItem("GameServer");
		serverCombo.addItem("CenterServer");
		serverCombo.addItem("ProxyServer");
		
		serverCombo.setSelectedItem("GameServer");
		
		serverCombo.addItemListener(new ServerComboBoxListener());
	}
	
	private class ServerComboBoxListener implements ItemListener {
		@Override
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() != ItemEvent.SELECTED) {
				return;
			}
			connectType = e.getItem().toString();
		}
	}
	
	public class SendButtonActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			String[] strs = selectedStr.split("-");
			int protocol = Integer.parseInt(strs[0]);
			Class<? extends GeneratedMessageLite> selectClz = protocolMap.get(protocol);
		
			try {
				Method newMethod = selectClz.getDeclaredMethod("newBuilder", new Class[0]);
				
				Object builder = newMethod.invoke(selectClz, new Object[0]);
				
				Method buildMethod = builder.getClass().getDeclaredMethod("build", new Class[0]);
				
				String[] keyValues = SimulatorClient.getInstance().getInputText().split("\n");
				
				for(String str : keyValues) {
					if("".equals(str.trim())) {
						continue;
					}
					String[] nameValue = str.split("=");
					if(nameValue.length < 2) {
						continue;
					}
					Field field = builder.getClass().getDeclaredField(nameValue[0] + "_");
					Method setter = null;
					Class<?> parameterClass = null;
					if(field.getType().isAssignableFrom(String.class)) {
						parameterClass = String.class;
						setter = builder.getClass().getDeclaredMethod(
								"set" + nameValue[0].substring(0, 1).toUpperCase()
										+ nameValue[0].substring(1),
										parameterClass);
					} else if(field.getType().isAssignableFrom(List.class)) {
						System.out.println(field.getType()+"***");
						parameterClass = (Class<?>)((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];
						setter = builder.getClass().getDeclaredMethod(
								"add" + nameValue[0].substring(0, 1).toUpperCase()
										+ nameValue[0].substring(1),
										getMethodClass(parameterClass));
					}else if(field.getType().isEnum()){//如果是action的枚举
						int index = str.indexOf('=');
						int value = Integer.parseInt(str.substring(index+1, str.length()));
						try {
							setter = builder.getClass().getDeclaredMethod(
									"setAction", field.getType());
						} catch (NoSuchMethodException e2) {
							setter =  builder.getClass().getDeclaredMethod(
									"setOption", field.getType());
						}
							
							
							Method valueOf = field.getType().getMethod("valueOf",int.class);
					/*		for(int i=0;i<field.getType().getEnumConstants().length;i++){
								Field field2 = field.getType().getEnumConstants()[i].getClass().getDeclaredField("value");
								field2.setAccessible(true);
								System.out.println( field.getType().getEnumConstants()[i]);
							}
							for(int i=0;i<field.getType().getMethods().length;i++){
								System.out.println( field.getType().getMethod("valueOf", int.class));
							}*/
							setter.invoke(builder, valueOf.invoke(field.getType(),value));
					}else {
						parameterClass = field.getType();
						setter = builder.getClass().getDeclaredMethod(
								"set" + nameValue[0].substring(0, 1).toUpperCase()
										+ nameValue[0].substring(1),
										parameterClass);
					}
					if(field.getType().isAssignableFrom(String.class)) {
						setter.invoke(builder, nameValue[1]);
					} else if("int".equals(field.getType().getSimpleName())) {
						setter.invoke(builder, Integer.parseInt(nameValue[1]));
					} else if("long".equals(field.getType().getSimpleName())) {
						setter.invoke(builder, Long.parseLong(nameValue[1]));
					} else if(field.getType().isAssignableFrom(List.class)) {		
						//对该类进行特殊处理（先做傻瓜方法）***************************	
						//name:天天-phone:11|name:小李-phone:22|name:柯南-phone:33
						if(parameterClass.getName().equals("com.yaowan.protobuf.game.GFriend$GFamilarFriend")){
							GMsg_11004006.Builder bbb=GMsg_11004006.newBuilder();					
							System.out.println("com.yaowan.protobuf.game.GFriend$GMsg_11004006$Builder");
							String[] familarFriends=nameValue[1].split("\\|");
							for (String oneFriend : familarFriends) {
								GFamilarFriend.Builder familarFriend=GFamilarFriend.newBuilder();						
								String[] s=oneFriend.split("-");//获得name:天天           phone:11
								System.out.println(s[0].split(":")[1]);//获得名字   如天天
								System.out.println(s[1].split(":")[1]);//获得电话   如11
								String name=s[0].split(":")[1];
								int phone=Integer.parseInt(s[1].split(":")[1]);
								familarFriend.setName(name);
								familarFriend.setPhone(phone);
								bbb.addFamiliarPeopleList(familarFriend);
							}
							netty.write(bbb.build());
							return;
						}
						//对该类进行特殊处理（先做傻瓜方法）***************************		
						String[] values = nameValue[1].split(",");
						for(String value : values) {
							if(parameterClass == Long.class) {
								setter.invoke(builder, Long.parseLong(value));
							} else if(parameterClass == Integer.class) {
								setter.invoke(builder, Integer.parseInt(value));
							}else {
								setter.invoke(builder, value);
							}
						}
					}else if(! field.getType().isEnum()){
						/*****G_T_C*****构造出牌对象***********/
						System.out.println(field.getType());
						Method newbuilder = field.getType().getMethod(
								"newBuilder");
						Object builder2 = newbuilder.invoke(field.getType());
						Method[] methods = builder2.getClass().getMethods();
						String name = null;
						Method addAllMothod = null;
						for (int i = 0; i < methods.length; i++) {
							name = methods[i].getName();
							int index2 = name.indexOf("addAll");
							if (index2 != -1) {
								addAllMothod = methods[i];
								break;
							}

						}
						Method setMothod = null;
						for (int i = 0; i < methods.length; i++) {
							name = methods[i].getName();
							int index2 = name.indexOf("set");
							if (index2 != -1) {
								setMothod = methods[i];
								break;
							}

						}
						int index = str.indexOf('=');
						String[] strings = str.substring(index + 1,
								str.length()).split(",");
						List<Integer> integers = new ArrayList<>();
						for (int i = 0; i < strings.length; i++) {
							integers.add(Integer.parseInt(strings[i]));
						}
						addAllMothod.invoke(builder2, integers);
						if (integers.size() == 1) {
							int value = integers.get(0).intValue();
							System.out.println(value);
							setMothod.invoke(builder2, 0, value);
						}

						Method buildMethod2 = builder2.getClass()
								.getDeclaredMethod("build", new Class[0]);
						setter = builder.getClass().getDeclaredMethod(
								"set"
										+ nameValue[0].substring(0, 1)
												.toUpperCase()
										+ nameValue[0].substring(1),
								field.getType());
						setter.invoke(builder, buildMethod2.invoke(builder2));
					}
				}
				
				
				Object builded = buildMethod.invoke(builder, new Object[0]);
				SimulatorPrinter.print(builded);
				
				netty.write((GeneratedMessageLite)builded);
			} catch (Exception e1) {
				e1.printStackTrace();
			} 
		}
	}
	
	public static Class<?> getMethodClass(Class<?> clz) {
		if(clz == Integer.class) {
			return int.class;
		} else if(clz == Long.class) {
			return long.class;
		} else {
			return clz;
		}
	}
}
