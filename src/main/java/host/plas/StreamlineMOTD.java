package host.plas;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.modules.StreamlineModule;
import net.streamline.thebase.lib.pf4j.PluginWrapper;
import host.plas.events.MainListener;
import host.plas.ratapi.MOTDExpansion;
import host.plas.timers.MOTDRunner;

public class StreamlineMOTD extends StreamlineModule {
    @Getter @Setter
    private static StreamlineMOTD instance;

    @Getter @Setter
    private static MOTDConfig config;

    @Getter @Setter
    private static MOTDRunner runner;

    @Getter @Setter
    private static MainListener listener;

    @Getter @Setter
    private static MOTDExpansion expansion;

    @Override
    protected void registerCommands() {

    }

    public StreamlineMOTD(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void onEnable() {
        instance = this;

        config = new MOTDConfig();
        config.updateResponse();
        runner = new MOTDRunner();
        listener = new MainListener();

        ModuleUtils.listen(listener, this);

        expansion = new MOTDExpansion();
    }

    @Override
    public void onDisable() {

    }
}
