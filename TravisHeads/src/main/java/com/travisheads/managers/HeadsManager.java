package com.travisheads.managers;

import com.travisheads.TravisHeads;
import com.travisheads.models.PlayerHead;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class HeadsManager {

    private final TravisHeads plugin;
    private Connection connection;
    private final Object connectionLock = new Object();

    public HeadsManager(TravisHeads plugin) {
        this.plugin = plugin;
        setupDatabase();
    }

    private void setupDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + plugin.getDataFolder() + "/heads.db";
            connection = DriverManager.getConnection(url);
            
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA journal_mode=WAL");
                stmt.execute("PRAGMA synchronous=NORMAL");
                stmt.execute("PRAGMA cache_size=10000");
                stmt.execute("PRAGMA temp_store=MEMORY");

                String sql = "CREATE TABLE IF NOT EXISTS player_heads (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "uuid TEXT NOT NULL," +
                        "owner TEXT NOT NULL," +
                        "rarity TEXT NOT NULL," +
                        "timestamp LONG NOT NULL)";
                stmt.execute(sql);

                stmt.execute("CREATE INDEX IF NOT EXISTS idx_uuid ON player_heads(uuid)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_uuid_rarity ON player_heads(uuid, rarity)");
            }

            plugin.getLogger().info("Database SQLite carregada com sucesso!");
        } catch (ClassNotFoundException e) {
            plugin.getLogger().severe("Driver SQLite não encontrado: " + e.getMessage());
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao conectar database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Connection getConnection() throws SQLException {
        synchronized (connectionLock) {
            if (connection == null || connection.isClosed()) {
                String url = "jdbc:sqlite:" + plugin.getDataFolder() + "/heads.db";
                connection = DriverManager.getConnection(url);
            }
            return connection;
        }
    }

    public void addHead(Player player, PlayerHead head) {
        if (player == null || head == null) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                synchronized (connectionLock) {
                    try (PreparedStatement pstmt = getConnection().prepareStatement(
                            "INSERT INTO player_heads (uuid, owner, rarity, timestamp) VALUES (?, ?, ?, ?)")) {
                        
                        pstmt.setString(1, player.getUniqueId().toString());
                        pstmt.setString(2, head.getOwnerName());
                        pstmt.setString(3, head.getRarityId());
                        pstmt.setLong(4, head.getTimestamp());
                        pstmt.executeUpdate();
                        
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                plugin.getHeadsCache().invalidate(player);
                            }
                        }.runTask(plugin);

                    } catch (SQLException e) {
                        plugin.getLogger().severe("Erro ao adicionar head: " + e.getMessage());
                    }
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    public int getTotalHeads(Player player) {
        if (player == null) return 0;

        synchronized (connectionLock) {
            try (PreparedStatement pstmt = getConnection().prepareStatement(
                    "SELECT COUNT(*) as total FROM player_heads WHERE uuid = ?")) {
                
                pstmt.setString(1, player.getUniqueId().toString());
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("total");
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Erro ao contar heads: " + e.getMessage());
            }
        }
        return 0;
    }

    public Map<String, Integer> getHeadsByRarity(Player player) {
        if (player == null) return Collections.emptyMap();

        Map<String, Integer> rarityCount = new HashMap<>();
        
        synchronized (connectionLock) {
            try (PreparedStatement pstmt = getConnection().prepareStatement(
                    "SELECT rarity, COUNT(*) as count FROM player_heads WHERE uuid = ? GROUP BY rarity")) {
                
                pstmt.setString(1, player.getUniqueId().toString());
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        rarityCount.put(rs.getString("rarity"), rs.getInt("count"));
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Erro ao buscar heads por raridade: " + e.getMessage());
            }
        }
        return rarityCount;
    }

    public boolean removeHeads(Player player, String rarityId, int amount) {
        if (player == null || rarityId == null || amount <= 0) return false;

        synchronized (connectionLock) {
            try {
                Connection conn = getConnection();
                conn.setAutoCommit(false);

                try (PreparedStatement checkStmt = conn.prepareStatement(
                        "SELECT COUNT(*) as count FROM player_heads WHERE uuid = ? AND rarity = ?")) {
                    
                    checkStmt.setString(1, player.getUniqueId().toString());
                    checkStmt.setString(2, rarityId);
                    
                    int available;
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        available = rs.next() ? rs.getInt("count") : 0;
                    }

                    if (available < amount) {
                        conn.rollback();
                        conn.setAutoCommit(true);
                        return false;
                    }
                }

                try (PreparedStatement deleteStmt = conn.prepareStatement(
                        "DELETE FROM player_heads WHERE id IN " +
                        "(SELECT id FROM player_heads WHERE uuid = ? AND rarity = ? LIMIT ?)")) {
                    
                    deleteStmt.setString(1, player.getUniqueId().toString());
                    deleteStmt.setString(2, rarityId);
                    deleteStmt.setInt(3, amount);
                    deleteStmt.executeUpdate();
                }

                conn.commit();
                conn.setAutoCommit(true);
                
                plugin.getHeadsCache().invalidate(player);
                return true;

            } catch (SQLException e) {
                plugin.getLogger().severe("Erro ao remover heads: " + e.getMessage());
                try {
                    getConnection().rollback();
                    getConnection().setAutoCommit(true);
                } catch (SQLException ex) {
                    plugin.getLogger().severe("Erro no rollback: " + ex.getMessage());
                }
                return false;
            }
        }
    }

    public CompletableFuture<Integer> getTotalHeadsAsync(Player player) {
        return CompletableFuture.supplyAsync(() -> getTotalHeads(player));
    }

    public CompletableFuture<Map<String, Integer>> getHeadsByRarityAsync(Player player) {
        return CompletableFuture.supplyAsync(() -> getHeadsByRarity(player));
    }

    public void saveAll() {
        synchronized (connectionLock) {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.commit();
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Erro ao salvar dados: " + e.getMessage());
            }
        }
    }

    public void closeConnection() {
        synchronized (connectionLock) {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                    plugin.getLogger().info("Conexão SQLite fechada com sucesso!");
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Erro ao fechar conexão: " + e.getMessage());
            }
        }
    }
}
