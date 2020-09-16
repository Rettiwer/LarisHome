package com.rettiwer.pl.laris.data.local.room;

import com.rettiwer.pl.laris.data.remote.api.room.Room;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;

@Dao
public interface RoomDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insert(List<Room> rooms);

    @Update
    Completable update(Room room);

    @Query("SELECT * FROM rooms")
    Maybe<List<Room>> select();

    @Delete
    Single<Integer> delete(List<Room> rooms);

    @Query("DELETE FROM rooms")
    Single<Integer> deleteAll();
}
