package com.rettiwer.pl.laris.data.remote.api.sensors;

public class SwitchSensor extends Sensor {
    private boolean state;

    public SwitchSensor(String id, String name, String type, String icon) {
        super(id, name, type, icon);
    }

    public boolean getState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }
}
