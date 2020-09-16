package com.rettiwer.pl.laris.data.remote.api.room;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "rooms")
public class Room {
    @NonNull
    @PrimaryKey
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("icon")
    @Expose
    private String icon;
    @SerializedName("icon_color")
    @Expose
    private String iconColor;
    @Ignore
    @SerializedName("members")
    @Expose
    private List<String> members;

    public Room() { }

    public Room(String id, String name, String icon, String iconColor, List<String> members) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.iconColor = iconColor;
        this.members = members;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getIconColor() {
        return iconColor;
    }

    public void setIconColor(String iconColor) {
        this.iconColor = iconColor;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Room)) return false;
        Room k = (Room) o;
        return id.equals(k.getId());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(id.toCharArray());
    }
}
