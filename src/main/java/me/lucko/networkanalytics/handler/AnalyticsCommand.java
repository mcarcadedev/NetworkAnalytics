/*
 * This file is part of NetworkAnalytics, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package me.lucko.networkanalytics.handler;

import lombok.RequiredArgsConstructor;

import me.lucko.helper.Schedulers;
import me.lucko.helper.time.DurationFormatter;
import me.lucko.helper.time.Time;
import me.lucko.helper.utils.Players;
import me.lucko.networkanalytics.AnalyticsPlugin;
import me.lucko.networkanalytics.data.StatsHolder;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class AnalyticsCommand implements CommandExecutor {
    private final AnalyticsPlugin plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("networkanalytics.view")) {
            Players.msg(sender,"&3[ANALYTICS] &fYou do not have permission to use this command.");
            return true;
        }

        Players.msg(sender, "&3[ANALYTICS] &fRetrieving monitoring data...");

        plugin.getDataManager().getStats().thenAcceptAsync(statsHolder -> {
            if (!statsHolder.isPresent()) {
                Players.msg(sender, "&3[ANALYTICS] &fUnable to retrieve monitoring data.");
                return;
            }

            // the stats holder
            StatsHolder s = statsHolder.get();

            // the number of unique joins
            BigDecimal total = BigDecimal.valueOf(s.getUniqueJoins());

            List<String> messages = new ArrayList<>();

            messages.add("&f&m-&3&m-&f&m-&3&m-&f&m-&3&m-&f&m-&3&m-&f&m-&f[ &bAnalytics &f]&f&m-&3&m-&f&m-&3&m-&f&m-&3&m-&f&m-&3&m-&f&m-&r\n&r");
            messages.add("&fPlayer Retention:");
            messages.add("  &3- &fPlay time greater than 1h: &3" + formatPercent(total, s.getNumWithPtGreaterThan1h()));
            messages.add("  &3- &fPlay time greater than 6h: &3" + formatPercent(total, s.getNumWithPtGreaterThan6h()));
            messages.add("  &3- &fConnected more than 50 times: &3" + formatPercent(total, s.getNumWithConnGreaterThan50()));
            messages.add(" ");
            messages.add("  &3- &fLast login more than 1 month ago: &3" + formatPercent(total, s.getNumWithLastLoginMoreThan1moAgo()));
            messages.add("  &3- &fLast login more than 1 week ago: &3" + formatPercent(total, s.getNumWithLastLoginMoreThan1wAgo()));
            messages.add("  &3- &fConnected less than 10 times: &3" + formatPercent(total, s.getNumWithConnLessThan10()));
            messages.add("  &3- &fPlay time less than 30 minutes: &3" + formatPercent(total, s.getNumWithPtLessThan30m()));
            messages.add(" ");
            messages.add("  &3- &fAverage time played: &3" + DurationFormatter.format(Time.duration(TimeUnit.SECONDS, s.getAverageTimePlayed() * 60L), true));
            messages.add("  &3- &fAverage times connected: &3" + s.getAverageTimesConnected());
            messages.add(" ");
            messages.add("&fAll time:");
            messages.add("  &3- &fUnique joins: &3" + formatNumberShort(s.getUniqueJoins()));
            messages.add("  &3- &fTotal time played: &3" + DurationFormatter.format(Time.duration(TimeUnit.SECONDS, s.getTotalTimePlayed() * 60L), true));;
            messages.add("  &3- &fTotal connections: &3" + formatNumberShort(s.getTotalConnections()));
            messages.add(" ");
            messages.add("&fLast month:");
            messages.add("  &3- &fUnique joins: &3" + formatNumberShort(s.getUniqueJoinsMonth()));
            messages.add("  &3- &fNew players: &3" + formatNumberShort(s.getNewPlayersMonth()) + " &7(" + formatPercent(BigDecimal.valueOf(s.getUniqueJoinsMonth()), s.getNewPlayersMonth()) + "&7)");
            messages.add("  &3- &fReturning players: &3" + formatNumberShort(s.getReturningPlayersMonth()) + " &7(" + formatPercent(BigDecimal.valueOf(s.getUniqueJoinsMonth()), s.getReturningPlayersMonth()) + "&7)");
            messages.add("&fLast week:");
            messages.add("  &3- &fUnique joins: &3" + formatNumberShort(s.getUniqueJoinsWeek()));
            messages.add("  &3- &fNew players: &3" + formatNumberShort(s.getNewPlayersWeek()) + " &7(" + formatPercent(BigDecimal.valueOf(s.getUniqueJoinsWeek()), s.getNewPlayersWeek()) + "&7)");
            messages.add("  &3- &fReturning players: &3" + formatNumberShort(s.getReturningPlayersWeek()) + " &7(" + formatPercent(BigDecimal.valueOf(s.getUniqueJoinsWeek()), s.getReturningPlayersWeek()) + "&7)");
            messages.add("&fLast 24 hours:");
            messages.add("  &3- &fUnique joins: &3" + formatNumberShort(s.getUniqueJoinsToday()));
            messages.add("  &3- &fNew players: &3" + formatNumberShort(s.getNewPlayersToday()) + " &7(" + formatPercent(BigDecimal.valueOf(s.getUniqueJoinsToday()), s.getNewPlayersToday()) + "&7)");
            messages.add("  &3- &fReturning players: &3" + formatNumberShort(s.getReturningPlayersToday()) + " &7(" + formatPercent(BigDecimal.valueOf(s.getUniqueJoinsToday()), s.getReturningPlayersToday()) + "&7)");
            messages.add(" ");

            for (String str : messages) {
                Players.msg(sender, str);
            }

        }, Schedulers.async());
        return true;
    }

    private static String formatPercent(BigDecimal total, long quot) {
        return BigDecimal.valueOf(quot).multiply(BigDecimal.valueOf(100)).divide(total, BigDecimal.ROUND_HALF_UP).round(new MathContext(3, RoundingMode.HALF_UP)).toPlainString() + "%";
    }

    private static String formatNumberShort(long num) {
        if (num >= 1000000000000000L) {
            return (Math.floor(((float) num / 1000000000000000f) * 10f) / 10d) + "Q";
        }
        if (num >= 1000000000000L) {
            return (Math.floor(((float) num / 1000000000000f) * 10f) / 10d) + "T";
        }
        if (num >= 1000000000L) {
            return (Math.floor(((float) num / 1000000000f) * 10f) / 10d) + "B";
        }
        if (num >= 1000000) {
            return (Math.floor(((float) num / 1000000L) * 10f) / 10d) + "M";
        }
        return NumberFormat.getNumberInstance(Locale.US).format(num);
    }
}
