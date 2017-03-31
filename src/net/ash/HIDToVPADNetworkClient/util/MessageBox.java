package net.ash.HIDToVPADNetworkClient.util;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import lombok.Getter;

public class MessageBox {
	public static final int MESSAGE_INFO = JOptionPane.INFORMATION_MESSAGE;
	public static final int MESSAGE_WARNING = JOptionPane.WARNING_MESSAGE;
	public static final int MESSAGE_ERROR = JOptionPane.ERROR_MESSAGE;
	
	@Getter
	private String message;
	@Getter
	private int type;
	
	public MessageBox(String message, int type) {
		this.message = message;
		this.type = type;
	}
	
	private static List<MessageBox> messageBoxQueue = new ArrayList<MessageBox>();
	public static void show(MessageBox box) {
		messageBoxQueue.add(box);
	}
	public static MessageBox getNextMessage() {
		if (messageBoxQueue.size() > 0) {
			return messageBoxQueue.get(0);
		} else return null;
	}
	public static void bumpQueue() {
		if (messageBoxQueue.size() > 0) {
			messageBoxQueue.remove(0);
		}
	}
}
