package host.plas.events;

import gg.drak.thebase.events.BaseEventListener;
import gg.drak.thebase.events.processing.BaseProcessor;
import host.plas.events.own.MOTDSendEvent;
import host.plas.StreamlineMOTD;
import host.plas.config.MOTDConfig;
import singularity.events.server.ping.PingReceivedEvent;

public class MainListener implements BaseEventListener {
    @BaseProcessor
    public void onPing(PingReceivedEvent event) {
        if (StreamlineMOTD.getConfig().getUpdateType() == MOTDConfig.UpdateType.ON_PING) {
            StreamlineMOTD.getConfig().updateResponse();

            MOTDSendEvent sendEvent = new MOTDSendEvent(event).fire();
            if (sendEvent.isCancelled()) {
                return;
            }
        }

        event.setResponse(StreamlineMOTD.getConfig().build(event.getResponse()));
    }
}
