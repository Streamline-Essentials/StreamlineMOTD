package host.plas.events;

import singularity.events.server.ping.PingReceivedEvent;
import host.plas.StreamlineMOTD;
import host.plas.config.MOTDConfig;
import tv.quaint.events.BaseEventListener;
import tv.quaint.events.processing.BaseProcessor;

public class MainListener implements BaseEventListener {
    @BaseProcessor
    public void onPing(PingReceivedEvent event) {
        if (StreamlineMOTD.getConfig().getUpdateType() == MOTDConfig.UpdateType.ON_PING) {
            StreamlineMOTD.getConfig().updateResponse();
        }

        event.setResponse(StreamlineMOTD.getConfig().build(event.getResponse()));
    }
}
