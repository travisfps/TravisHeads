package com.travisheads.managers;

import com.travisheads.TravisHeads;
import com.travisheads.models.PlayerHead;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.*;

public class HeadsManager {

    private final TravisHeads plugin;
    private Connection connection;

    public HeadsManager(TravisHeads plugin) {
        this.plugin = plugin;
        setupDatabase();
    }

    private void setupDatabase() {
        try {
            String url = "jdbc:sqlite:" + plugin.getDataFolder() + "/heads.db";
            connection = DriverManager.getConnection(url);
            Statement stmt = connection.createStatement();

            String sql = "CREATE TABLE IF NOT EXISTS player_heads (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "uuid TEXT NOT NULL," +
                    "owner TEXT NOT NULL," +
                    "rarity TEXT NOT NULL," +
                    "timestamp LONG NOT NULL)";
            stmt.execute(sql);

            stmt.execute("CREATE INDEX IF NOT EXISTS idx_uuid ON player_heads(uuid)");
            stmt.close();

            plugin.getLogger().info("Database SQLite carregada com sucesso!");
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao conectar database: " + e.getMessage());
        }
    }

    public void addHead(Player player, PlayerHead head) {
        try {
            String sql = "INSERT INTO player_heads (uuid, owner, rarity, timestamp) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, player.getUniqueId().toString());
            pstmt.setString(2, head.getOwnerName());
            pstmt.setString(3, head.getRarityId());
            pstmt.setLong(4, head.getTimestamp());
            pstmt.executeUpdate();
            pstmt.close();

            plugin.getHeadsCache().invalidate(player);

        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao adicionar head: " + e.getMessage());
        }
    }

    public int getTotalHeads(Player player) {
        try {
            String sql = "SELECT COUNT(*) as total FROM player_heads WHERE uuid = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, player.getUniqueId().toString());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int total = rs.getInt("total");
                rs.close();
                pstmt.close();
                return total;
            }

            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao contar heads: " + e.getMessage());
        }
        return 0;
    }

    public Map<String, Integer> getHeadsByRarity(Player player) {
        Map<String, Integer> rarityCount = new HashMap<>();
        try {
            String sql = "SELECT rarity, COUNT(*) as count FROM player_heads WHERE uuid = ? GROUP BY rarity";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, player.getUniqueId().toString());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String rarity = rs.getString("rarity");
                int count = rs.getInt("count");
                rarityCount.put(rarity, count);
            }

            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao buscar heads por raridade: " + e.getMessage());
        }
        return rarityCount;
    }

    public boolean removeHeads(Player player, String rarityId, int amount) {
        try {
            String checkSql = "SELECT COUNT(*) as count FROM player_heads WHERE uuid = ? AND rarity = ?";
            PreparedStatement checkStmt = connection.prepareStatement(checkSql);
            checkStmt.setString(1, player.getUniqueId().toString());
            checkStmt.setString(2, rarityId);
            ResultSet rs = checkStmt.executeQuery();

            int available = 0;
            if (rs.next()) {
                available = rs.getInt("count");
            }
            rs.close();
            checkStmt.close();

            if (available < amount) {
                return false;
            }

            String deleteSql = "DELETE FROM player_heads WHERE id IN " +
                    "(SELECT id FROM player_heads WHERE uuid = ? AND rarity = ? LIMIT ?)";
            PreparedStatement deleteStmt = connection.prepareStatement(deleteSql);
            deleteStmt.setString(1, player.getUniqueId().toString());
            deleteStmt.setString(2, rarityId);
            deleteStmt.setInt(3, amount);
            deleteStmt.executeUpdate();
            deleteStmt.close();

            plugin.getHeadsCache().invalidate(player);

            return true;
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao remover heads: " + e.getMessage());
            return false;
        }
    }

    public void saveAll() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.commit();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao salvar dados: " + e.getMessage());
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao fechar conexão: " + e.getMessage());
        }
    }
}