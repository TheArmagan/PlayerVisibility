package dev.xnasuni.playervisibility.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.xnasuni.playervisibility.PlayerVisibility;
import dev.xnasuni.playervisibility.config.ModConfig;
import dev.xnasuni.playervisibility.types.FilterType;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class VisibilityCommand {

    public static void Register(CommandDispatcher<FabricClientCommandSource> Dispatcher) {
        SuggestionProvider<FabricClientCommandSource> UsernameSuggestionProvider = (context, builder) -> {
            String input = builder.getRemaining().toLowerCase();

            for (String Suggestion : PlayerVisibility.getFilteredPlayers()) {
                if (Suggestion.toLowerCase().startsWith(input)) {
                    builder.suggest(Suggestion);
                }
            }

            return builder.buildFuture();
        };

        Dispatcher.register(literal("visibility")
                .then(literal("filter")
                        .then(literal("type")
                                .then(literal("whitelist")
                                        .executes(ctx -> {
                                            PlayerVisibility.setFilterType(FilterType.WHITELIST);
                                            PlayerVisibility.minecraftClient.player.sendMessage(Text.of("Filter mode is set to WHITELIST"), true);
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                                .then(literal("blacklist")
                                        .executes(ctx -> {
                                            PlayerVisibility.setFilterType(FilterType.BLACKLIST);
                                            PlayerVisibility.minecraftClient.player.sendMessage(Text.of("Filter mode is set to BLACKLIST"), true);
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                            ).then(literal("add")
                                        .then(argument("username", string())
                                                .executes(ctx -> {
                                                    String Username = getString(ctx, "username");
                                                    PlayerVisibility.addPlayerToFilter(Username);
                                                    return Command.SINGLE_SUCCESS;
                                                })
                                        )
                                )
                                .then(literal("remove")
                                        .then(argument("username", string())
                                                .suggests(UsernameSuggestionProvider)
                                                .executes(ctx -> {
                                                    String Username = getString(ctx, "username");
                                                    PlayerVisibility.removePlayerFromFilter(Username);
                                                    return Command.SINGLE_SUCCESS;
                                                })
                                        )
                                )
                                .then(literal("clear")
                                        .executes(ctx -> {
                                            PlayerVisibility.clearFilter();
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                                .then(literal("view")
                                        .executes(ctx -> {
                                            StringBuilder stringBuilder = new StringBuilder();
                                            for (String filtered : PlayerVisibility.getFilteredPlayers()) {
                                                if (stringBuilder.toString().equals("")) {
                                                    stringBuilder.append(String.format("§%c'%s'§f", ModConfig.INSTANCE.MainColor.GetChar(), filtered));
                                                } else {
                                                    stringBuilder.append(String.format(", §%c'%s'§f", ModConfig.INSTANCE.MainColor.GetChar(), filtered));
                                                }
                                            }

                                            stringBuilder.append(String.format(" §f(§%c%s§f)", ModConfig.INSTANCE.MainColor.GetChar(), PlayerVisibility.getFilteredPlayers().length));

                                            PlayerVisibility.minecraftClient.player.sendMessage(Text.of(stringBuilder.toString()), true);
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )

                .then(literal("toggle")
                        .executes(ctx -> {
                            PlayerVisibility.toggleFilter();
                            return Command.SINGLE_SUCCESS;
                        })
                )
        );
    }
}
