package host.plas.timers;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.scheduler.ModuleRunnable;
import host.plas.StreamlineMOTD;
import host.plas.config.MOTDConfig;

@Setter
@Getter
public class MOTDRunner extends ModuleRunnable {
    int runningTicks = 0;

    public MOTDRunner() {
        super(StreamlineMOTD.getInstance(), 0, 1);

        setRunningTicks(0);
    }

    @Override
    public void run() {
        StreamlineMOTD.getConfig().updatePlayers();

        if (StreamlineMOTD.getConfig().getUpdateType() == MOTDConfig.UpdateType.ON_TICK) {
            if (getRunningTicks() % StreamlineMOTD.getConfig().getMotdTicks() == 0) {
                StreamlineMOTD.getConfig().updateMotd();
            }
            if (getRunningTicks() % StreamlineMOTD.getConfig().getSampleTicks() == 0) {
                StreamlineMOTD.getConfig().updateSample();
            }
        }

        StreamlineMOTD.getConfig().updateVersion();

        StreamlineMOTD.getConfig().updateFavicon();

        setRunningTicks(getRunningTicks() + 1);
    }
}
