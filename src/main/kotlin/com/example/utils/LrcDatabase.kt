package com.example.utils

import com.example.data.LrcEntity
import com.example.data.LrcSearchEntity
import com.example.extension.toMd5String
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.io.File
import java.sql.DriverManager
import java.sql.Statement
import java.util.*
import kotlin.collections.ArrayList

class LrcDatabase {
    private var statement: Statement

    companion object {
        val instance by lazy { LrcDatabase() }
    }

    init {
        statement = createDatabase()
        createTable()
    }

    private fun createDatabase(): Statement {
        val path = FileUtil.getDataDirectory()
        val database = File(path, "data.db")
        val connection = DriverManager.getConnection("jdbc:sqlite:${database.absolutePath}")
        return connection.createStatement()
    }

    private fun createTable() {
        statement.execute("CREATE TABLE IF NOT EXISTS lrc (hash text PRIMARY KEY,songName text,artist text,duration integer,lrcValue text ,time integer)")
    }

    suspend fun delete(hash: String): Boolean = withContext(Dispatchers.IO) {
        try {
            statement.execute("delete from lrc where hash = '$hash'")
            LoggerFactory.getLogger("Lrc").info("delete succeed: $hash")
            return@withContext true
        } catch (e: Exception) {
            LoggerFactory.getLogger("Lrc").error("delete error: $hash")
            LoggerFactory.getLogger("Lrc").error("delete error: ${e.stackTraceToString()}")
            return@withContext false
        }
    }

    suspend fun put(artist: String, songName: String, duration: Long = 0, lrcValue: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val hash = (artist + songName + duration).toMd5String()
            val time = System.currentTimeMillis().toString()
            val encodeValue = Base64.getEncoder().encodeToString(lrcValue.encodeToByteArray())
            if (count(hash) > 0) {
                LoggerFactory.getLogger("Lrc").error("$artist - $songName - $duration >> hash:$hash already exist!")
                return@withContext false
            }
            statement.execute("replace into lrc values('$hash','$songName','$artist','$duration','$encodeValue','$time')")
            LoggerFactory.getLogger("Lrc").info("insert succeed: $artist - $songName - $duration >> $lrcValue")
            return@withContext true
        } catch (e: Exception) {
            LoggerFactory.getLogger("Lrc").error("insert error: $artist - $songName - $duration >> $lrcValue")
            LoggerFactory.getLogger("Lrc").error("insert error: ${e.stackTraceToString()}")
            return@withContext false
        }
    }

    suspend fun search(artist: String, songName: String): List<LrcSearchEntity> = withContext(Dispatchers.IO) {
        LoggerFactory.getLogger("Lrc").info("search: $artist - $songName")
        LoggerFactory.getLogger("Lrc").info("search: \"select * from lrc where artist like '%$artist%' or songName like '%$songName%' order by time limit 100 offset 0;\"")
        return@withContext ArrayList<LrcSearchEntity>().apply {
            try {
                val rs = statement.executeQuery("select * from lrc where artist like '%$artist%' or songName like '%$songName%' order by time limit 100 offset 0;")
                while (rs.next()) {
                    val hash = rs.getString("hash")
                    val artistN = rs.getString("artist")
                    val songNameN = rs.getString("songName")
                    val duration = rs.getLong("duration")
                    add(LrcSearchEntity(hash, artistN, songNameN, duration))
                }
            } catch (e: Exception) {
                LoggerFactory.getLogger("Lrc").error("search error: ${e.stackTraceToString()}")
            }
        }
    }

    suspend fun get(hash: String): LrcEntity? = withContext(Dispatchers.IO) {
        try {
            val rs = statement.executeQuery("select * from lrc where hash = '$hash';")
            if (rs.next()) {
                val artistN = rs.getString("artist")
                val songNameN = rs.getString("songName")
                val lrcValue = rs.getString("lrcValue")
                val duration = rs.getLong("duration")
                val value = Base64.getDecoder().decode(lrcValue).decodeToString()
                return@withContext LrcEntity(artistN, songNameN, duration, value)
            } else {
                return@withContext null
            }
        } catch (e: Exception) {
            LoggerFactory.getLogger("Lrc").error("get error: ${e.stackTraceToString()}")
            return@withContext null
        }
    }

    suspend fun count(): Int = withContext(Dispatchers.IO) {
        val rs = statement.executeQuery("select count(*) from lrc;")
        while (rs.next()) {
            return@withContext rs.getInt(1)
        }
        return@withContext 0
    }

    suspend fun count(hash: String): Int = withContext(Dispatchers.IO) {
        val rs = statement.executeQuery("select count(*) from lrc where hash = '$hash';")
        while (rs.next()) {
            return@withContext rs.getInt(1)
        }
        return@withContext 0
    }

    suspend fun clear(): Int = withContext(Dispatchers.IO) {
        statement.executeUpdate("delete from lrc")
    }
}