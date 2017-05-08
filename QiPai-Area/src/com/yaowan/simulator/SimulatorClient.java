/**
 * 
 */
package com.yaowan.simulator;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * @author huangyuyuan
 *
 */
public class SimulatorClient extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static SimulatorClient client = new SimulatorClient();
	
	public static SimulatorClient getInstance() {
		return client;
	}

	public static void main(String[] args) {
		getInstance().setVisible(true);
	}
	
	private SimulatorClient() {
		init();
		initCompenent();
		
		input();
		result();
		//占位
		this.add(new JPanel());
	}
	
	public void add(JPanel panel, Component c, GridBagConstraints constraints, int x, int y, int w, int h) {
		constraints.gridx = x;
		constraints.gridy = y;
		constraints.gridwidth = w;
		constraints.gridheight = h;
		constraints.anchor = GridBagConstraints.CENTER;
		panel.add(c, constraints);
	}
	
	private JTextArea result;
	private JTextArea input;
	
	public void initCompenent() {
		JPanel panel = new JPanel();
		panel.setBounds(0, 0, 280, 200);
		
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.weighty = 8;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		
		GridBagLayout gridLayout = new GridBagLayout();
		panel.setLayout(gridLayout);
		
		SimulatorGamePanel gamePanel = new SimulatorGamePanel();
		add(panel, gamePanel, constraints, 0, 1, 5, 3);

		this.add(panel);
	}
	
	public void init() {
		int width = 1000;
		int height = 700;
		
		//设置窗口大小与居中
		setBounds((SimulatorConfig.SCREEN_WIDTH - width) / 2, (SimulatorConfig.SCREEN_HEIGHT - height - 60) / 2, width, height);

		// 禁止改变窗体大小
		setResizable(false);
		
		// 点击关闭按钮关闭程序
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public void input() {
		JPanel panel = new JPanel();
		panel.setBounds(0, 200, 280, 470);
		input = new JTextArea();
		
		JScrollPane scroll = new JScrollPane();
		scroll.setPreferredSize(new Dimension(279, 460));
		scroll.setViewportView(input);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		
		panel.add(scroll);
		this.add(panel);
	}
	
	public void addResultText(String text) {
		result.append(text);
		result.selectAll();
	}
	
	public void setInputText(String text) {
		input.setText(text);
	}
	
	public String getInputText() {
		return input.getText();
	}
	
	private void result() {
		JPanel panel = new JPanel();
		panel.setBounds(280, 0, 712, 670);
		
		result = new JTextArea();
		result.setLineWrap(true);
		JScrollPane scroll = new JScrollPane();
		scroll.setPreferredSize(new Dimension(702, 660));
		scroll.setViewportView(result);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		panel.add(scroll);
		this.add(panel);
	}
}
