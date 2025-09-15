package host.plas.timers;

import host.plas.data.UpdateType;
import lombok.Getter;
import lombok.Setter;
import singularity.scheduler.ModuleRunnable;
import host.plas.StreamlineMOTD;

@Setter
@Getter
public class MOTDRunner extends ModuleRunnable {
    long runningTicks = 0;

    public MOTDRunner() {
        super(StreamlineMOTD.getInstance(), 0, getTicks());

        setRunningTicks(0);
    }

    public static long getTicks() {
        return StreamlineMOTD.getConfig().getUpdateTicks();
    }

    @Override
    public void run() {
        if (getPeriod() != getTicks()) {
            setPeriod(getTicks());
        }

        StreamlineMOTD.getConfig().updatePlayers();

        if (StreamlineMOTD.getConfig().getUpdateType() == UpdateType.ON_TICK && getTicks() > 0) {
            if (StreamlineMOTD.getConfig().getMotdTicks() != -1 && getRunningTicks() % StreamlineMOTD.getConfig().getMotdTicks() == 0) {
                StreamlineMOTD.getConfig().updateMotd();
            }
            if (StreamlineMOTD.getConfig().getSampleTicks() != -1 && getRunningTicks() % StreamlineMOTD.getConfig().getSampleTicks() == 0) {
                StreamlineMOTD.getConfig().updateSample();
            }
        }

        StreamlineMOTD.getConfig().updateVersion();

        StreamlineMOTD.getConfig().updateFavicon();

        setRunningTicks(getRunningTicks() + 1);
    }
}
