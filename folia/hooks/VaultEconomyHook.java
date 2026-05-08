package com.bx.ultimateDonutSmp.hooks;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.EconomyReason;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import net.milkbowl.vault.economy.AbstractEconomy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.List;

public class VaultEconomyHook extends AbstractEconomy {

    private final UltimateDonutSmp plugin;

    public VaultEconomyHook(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isEnabled() {
        return plugin.isEnabled();
    }

    @Override
    public String getName() {
        return plugin.getPluginMeta().getName();
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public int fractionalDigits() {
        return 2;
    }

    @Override
    public String format(double amount) {
        return "$" + NumberUtils.format(amount);
    }

    @Override
    public String currencyNamePlural() {
        return "dollars";
    }

    @Override
    public String currencyNameSingular() {
        return "dollar";
    }

    @Override
    public boolean hasAccount(String playerName) {
        return plugin.getEconomyManager().resolveAccount(playerName) != null;
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return plugin.getEconomyManager().hasAccount(player);
    }

    @Override
    public boolean hasAccount(String playerName, String worldName) {
        return hasAccount(playerName);
    }

    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        return hasAccount(player);
    }

    @Override
    public double getBalance(String playerName) {
        var account = plugin.getEconomyManager().resolveAccount(playerName);
        return account == null ? 0D : plugin.getEconomyManager().getBalance(account.uuid());
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return plugin.getEconomyManager().getBalance(player);
    }

    @Override
    public double getBalance(String playerName, String world) {
        return getBalance(playerName);
    }

    @Override
    public double getBalance(OfflinePlayer player, String world) {
        return getBalance(player);
    }

    @Override
    public boolean has(String playerName, double amount) {
        var account = plugin.getEconomyManager().resolveAccount(playerName);
        return account != null && plugin.getEconomyManager().has(account.uuid(), amount);
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return plugin.getEconomyManager().has(player, amount);
    }

    @Override
    public boolean has(String playerName, String worldName, double amount) {
        return has(playerName, amount);
    }

    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        return has(player, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        var account = plugin.getEconomyManager().resolveAccount(playerName);
        if (account == null) {
            return failureResponse(amount, 0D, "Player account not found.");
        }

        var result = plugin.getEconomyManager().withdraw(account, amount, EconomyReason.AUCTION_PURCHASE);
        return toResponse(result);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        return toResponse(plugin.getEconomyManager().withdraw(player, amount, EconomyReason.AUCTION_PURCHASE));
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return withdrawPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return withdrawPlayer(player, amount);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        var account = plugin.getEconomyManager().resolveAccount(playerName);
        if (account == null) {
            return failureResponse(amount, 0D, "Player account not found.");
        }

        var result = plugin.getEconomyManager().deposit(account, amount, EconomyReason.SELL_PAYOUT);
        return toResponse(result);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        return toResponse(plugin.getEconomyManager().deposit(player, amount, EconomyReason.SELL_PAYOUT));
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        return depositPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return depositPlayer(player, amount);
    }

    @Override
    public EconomyResponse createBank(String name, String player) {
        return bankUnsupported();
    }

    @Override
    public EconomyResponse createBank(String name, OfflinePlayer player) {
        return bankUnsupported();
    }

    @Override
    public EconomyResponse deleteBank(String name) {
        return bankUnsupported();
    }

    @Override
    public EconomyResponse bankBalance(String name) {
        return bankUnsupported();
    }

    @Override
    public EconomyResponse bankHas(String name, double amount) {
        return bankUnsupported();
    }

    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        return bankUnsupported();
    }

    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        return bankUnsupported();
    }

    @Override
    public EconomyResponse isBankOwner(String name, String playerName) {
        return bankUnsupported();
    }

    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
        return bankUnsupported();
    }

    @Override
    public EconomyResponse isBankMember(String name, String playerName) {
        return bankUnsupported();
    }

    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer player) {
        return bankUnsupported();
    }

    @Override
    public List<String> getBanks() {
        return List.of();
    }

    @Override
    public boolean createPlayerAccount(String playerName) {
        var account = plugin.getEconomyManager().resolveAccount(playerName);
        return account != null || plugin.getEconomyManager().createPlayerAccount(Bukkit.getOfflinePlayer(playerName));
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        return plugin.getEconomyManager().createPlayerAccount(player);
    }

    @Override
    public boolean createPlayerAccount(String playerName, String worldName) {
        return createPlayerAccount(playerName);
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        return createPlayerAccount(player);
    }

    private EconomyResponse toResponse(com.bx.ultimateDonutSmp.models.EconomyTransactionResult result) {
        if (result.success()) {
            return new EconomyResponse(
                    result.amount(),
                    result.afterBalance(),
                    EconomyResponse.ResponseType.SUCCESS,
                    null
            );
        }

        return failureResponse(result.amount(), result.beforeBalance(), result.failureReason().name());
    }

    private EconomyResponse failureResponse(double amount, double balance, String message) {
        return new EconomyResponse(
                amount,
                balance,
                EconomyResponse.ResponseType.FAILURE,
                message
        );
    }

    private EconomyResponse bankUnsupported() {
        return new EconomyResponse(0D, 0D, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support is not implemented.");
    }
}
