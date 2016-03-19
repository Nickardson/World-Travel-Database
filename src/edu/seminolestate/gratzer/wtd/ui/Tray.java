package edu.seminolestate.gratzer.wtd.ui;

import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import edu.seminolestate.gratzer.wtd.Main;

public class Tray {
	public Tray() {
		if (!SystemTray.isSupported()) {
			System.out.println("SystemTray is not supported");
			return;
		}

		PopupMenu popup = new PopupMenu();
		TrayIcon trayIcon = new TrayIcon(Toolkit.getDefaultToolkit().getImage(
				Tray.class.getResource("/resources/img/globe-green.png")));
		SystemTray tray = SystemTray.getSystemTray();

		// Add components to pop-up menu
		MenuItem exitItem = new MenuItem("Exit");
		exitItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Main.exit();
			}
		});
		popup.add(exitItem);

		trayIcon.setPopupMenu(popup);

		try {
			tray.add(trayIcon);
		} catch (AWTException e) {
			System.out.println("TrayIcon could not be added.");
		}
	}
}
