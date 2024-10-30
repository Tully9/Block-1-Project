package org.example.block1project;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import oshi.SystemInfo;
import oshi.hardware.NetworkIF;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class NetworkUsage {

    private ScrollPane scrollableNetworkInfo;
    private VBox networkInfoBox;
    private Map<NetworkIF, Label> networkSpeedLabels;
    private SystemInfo systemInfo;
    private Timer timer;

    // setup network usage display
    public NetworkUsage() {
        systemInfo = new SystemInfo();
        List<NetworkIF> networkIFs = systemInfo.getHardware().getNetworkIFs();

        networkSpeedLabels = new HashMap<>();
        VBox infoLabels = new VBox(10);

        for (NetworkIF net : networkIFs) {
            Label nameLabel = new Label("name: " + net.getDisplayName());
            Label macLabel = new Label("mac address: " + net.getMacaddr());
            Label ipv4Label = new Label("ipv4: " + String.join(", ", net.getIPv4addr()));
            Label ipv6Label = new Label("ipv6: " + String.join(", ", net.getIPv6addr()));

            nameLabel.setStyle("-fx-text-fill: black;");
            macLabel.setStyle("-fx-text-fill: black;");
            ipv4Label.setStyle("-fx-text-fill: black;");
            ipv6Label.setStyle("-fx-text-fill: black;");

            Label speedLabel = new Label("current speed: 0 kb/s");
            speedLabel.setStyle("-fx-text-fill: black;");
            networkSpeedLabels.put(net, speedLabel);

            VBox interfaceInfo = new VBox(5, nameLabel, macLabel, ipv4Label, ipv6Label, speedLabel);
            interfaceInfo.setPadding(new Insets(10));
            infoLabels.getChildren().add(interfaceInfo);
        }

        networkInfoBox = new VBox(10, new Label("network interface information"), infoLabels);
        networkInfoBox.setPadding(new Insets(20));
        networkInfoBox.setAlignment(Pos.CENTER);
        networkInfoBox.setStyle("-fx-background-color: #dddddd; -fx-font-weight: bold;");

        scrollableNetworkInfo = new ScrollPane(networkInfoBox);
        scrollableNetworkInfo.setFitToWidth(true);
        scrollableNetworkInfo.setPadding(new Insets(10));
        scrollableNetworkInfo.setStyle("-fx-background: #2b2b2b; -fx-border-color: transparent; -fx-background-radius: 10;");

        startNetworkUsageMonitoring();
    }

    // monitor and update network speed
    private void startNetworkUsageMonitoring() {
        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                for (NetworkIF net : networkSpeedLabels.keySet()) {
                    net.updateAttributes();
                    long bytesReceived = net.getBytesRecv();
                    long bytesSent = net.getBytesSent();
                    double combinedSpeed = (bytesReceived + bytesSent) / 1024.0;
                    Platform.runLater(() -> networkSpeedLabels.get(net).setText(String.format("current speed: %.2f kb/s", combinedSpeed)));
                }
            }
        }, 0, 1000);
    }

    public ScrollPane getNetworkUsageInfo() {
        return scrollableNetworkInfo;
    }

    // stop timer
    public void stopMonitoring() {
        if (timer != null) {
            timer.cancel();
        }
    }
}