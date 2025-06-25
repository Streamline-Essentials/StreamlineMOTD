package host.plas.config;

import gg.drak.thebase.storage.resources.flat.simple.SimpleConfiguration;
import gg.drak.thebase.utils.MathUtils;
import host.plas.StreamlineMOTD;
import lombok.Getter;
import lombok.Setter;
import net.streamline.api.SLAPI;
import singularity.modules.ModuleUtils;
import singularity.objects.CosmicFavicon;
import singularity.objects.PingedResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Setter
@Getter
public class MOTDConfig extends SimpleConfiguration {
    public enum SelectionType {
        RANDOM,
        SEQUENTIAL,
        ;
    }

    public enum UpdateType {
        ON_TICK,
        ON_PING,
        ;
    }

    private PingedResponse.Players sample;
    private PingedResponse.Protocol version;
    private String description;
    private int currentSample, currentMotd = 0;

    private String currentSampleText;
    private int maxPlayersValue, onlinePlayersValue = 0;
    private CosmicFavicon favicon;

    public MOTDConfig() {
        super("config.yml", StreamlineMOTD.getInstance().getDataFolder(), true);
    }

    @Override
    public void init() {
        getMotdList();
        getMotdSelection();
        getMotdTicks();

        getSampleList();
        getSampleSelection();
        getSampleTicks();

        maxPlayers();
        onlinePlayers();

        versionName();
        versionProtocol();

        getUpdateType();

        isDescriptionEnabled();
        isSampleEnabled();
        isVersionEnabled();
        isFaviconEnabled();

        getIconFolder();
        getFaviconFilePath();

        updateResponse();
    }

    public File getIconFolder() {
        File folder = new File(StreamlineMOTD.getInstance().getDataFolder(), "icons");

        if (! folder.exists()) {
            folder.mkdirs();
        }

        return folder;
    }

    public PingedResponse build(PingedResponse from) {
        if (getCurrentSampleText() == null) updateSample();

        setSample(ModuleUtils.codedString(ModuleUtils.replacePlaceholders(getCurrentSampleText())), getMaxPlayersValue(), getOnlinePlayersValue());
        if (isSampleEnabled()) {
            if (getSample() != null) {
                from.setPlayers(getSample());
            } else {
                from.setPlayers(from.getPlayers());
            }
        }

        if (isVersionEnabled()) {
            if (getVersion() != null) {
                from.setVersion(getVersion());
            } else {
                from.setVersion(from.getVersion());
            }
        }
        if (isDescriptionEnabled()) {
            if (getDescription() != null) {
                from.setDescription(ModuleUtils.codedString(ModuleUtils.replacePlaceholders(getDescription())));
            } else {
                from.setDescription(ModuleUtils.codedString(ModuleUtils.replacePlaceholders(from.getDescription())));
            }
        }
        if (isFaviconEnabled()) {
            try {
                if (getFavicon() != null) {
                    from.setFavicon(getFavicon());
                } else {
                    from.setFavicon(from.getFavicon());
                }
            } catch (Exception e) {
                StreamlineMOTD.getInstance().logWarning(e.getStackTrace());
            }
        }

        return from;
    }

    public void updateResponse() {
        CompletableFuture.runAsync(() -> {
            updateFavicon();

            updateMotd();

            updateSample();

            updateVersion();
        });
    }

    public void updateMotd() {
        CompletableFuture.runAsync(() -> {
            List<String> list = getMotdList();
            if (list != null) {
                if (!list.isEmpty()) {
                    switch (getMotdSelection()) {
                        case RANDOM:
                            Random RNG = new Random();
                            setDescription(ModuleUtils.codedString(ModuleUtils.replacePlaceholders(list.get(RNG.nextInt(list.size())))));
                            break;
                        case SEQUENTIAL:
                            if (currentMotd >= list.size()) {
                                currentMotd = 0;
                            }
                            setDescription(ModuleUtils.codedString(ModuleUtils.replacePlaceholders(list.get(currentMotd))));

                            currentMotd++;
                            break;
                    }
                }
            }
        });
    }

    public void updateSample() {
        CompletableFuture.runAsync(() -> {
            List<String> sampleList = getSampleList();
            if (sampleList != null) {
                if (! sampleList.isEmpty()) {
                    switch (getSampleSelection()) {
                        case RANDOM:
                            Random RNG = new Random();
                            setCurrentSampleText(ModuleUtils.codedString(ModuleUtils.replacePlaceholders(sampleList.get(RNG.nextInt(sampleList.size())))));
                            break;
                        case SEQUENTIAL:
                            if (currentSample >= sampleList.size()) {
                                currentSample = 0;
                            }
                            setCurrentSampleText(ModuleUtils.codedString(ModuleUtils.replacePlaceholders(sampleList.get(currentSample))));

                            currentSample ++;
                            break;
                    }
                }
            }
        });
    }

    public void updateVersion() {
        if (isVersionEnabled()) {
            int v = -1;
            if (getVersionProtocol() != null) {
                try {
                    v = Integer.parseInt(getVersionProtocol());
                } catch (Exception e) {
                    StreamlineMOTD.getInstance().logDebug(e.getStackTrace());
                }
            }

//            if (v == -1) {
//                setVersion(null);
//                return;
//            }

            PingedResponse.Protocol version = new PingedResponse.Protocol(ModuleUtils.codedString(ModuleUtils.replacePlaceholders(versionName())), v);
            setVersion(version);
        }
    }

    public void updatePlayers() {
        setMaxPlayersValue(getMaxPlayers());
        setOnlinePlayersValue(getOnlinePlayers());
    }

    public void updateFavicon() {
        String path = getFaviconFilePath();
        if (path != null) {
            if (! path.isEmpty()) {
                if (! path.equalsIgnoreCase("default")) {
                    try {
                        setFavicon(getFavicon(path));
                    } catch (Exception e) {
                        StreamlineMOTD.getInstance().logWarning(e.getStackTrace());
                    }
                } else {
                    setFavicon(null);
                }
            }
        }
    }

    public String getNewestDownloadIcon(String name, int iteration) {
        if (name == null) name = "downloaded.png";

        File f = new File(getIconFolder(), name);
        if (f.exists()) {
            return f.getName();
        }

        if (iteration > 100) {
            return null;
        }

        String newName = name;
        if (name.contains(".")) {
            String[] split = name.split("\\.");
            newName = split[0] + "-" + iteration + "." + split[1];
        } else {
            newName = name + "-" + iteration;
        }

        return getNewestDownloadIcon(newName, iteration + 1);
    }

    public CosmicFavicon getFavicon(String path) {
        try {
            URL url = null;
            if (path.contains("://")) {
                url = new URI(path).toURL();

                // Download favicon if it doesn't exist
                // to the icons folder
                File f = new File(getIconFolder(), path);
                if (! f.exists()) {
                    // Code to download the favicon and save it to the specified folder
                    // This could involve setting up a URL connection, reading the data,
                    // and writing it to the file
                    try {
                        URL faviconUrl = new URL(url, "/downloaded.png");
                        try (InputStream in = faviconUrl.openStream();
                             FileOutputStream out = new FileOutputStream(f)) {
                            byte[] buffer = new byte[1024];
                            int bytesRead;
                            while ((bytesRead = in.read(buffer)) != -1) {
                                out.write(buffer, 0, bytesRead);
                            }
                        }
                    } catch (IOException e) {
                        // Handle exceptions, e.g., favicon not found, read/write errors
                        StreamlineMOTD.getInstance().logWarning(e.getStackTrace());
                    }
                }
            }
            if (! path.contains(File.pathSeparator)) {
                File f = new File(getIconFolder(), path);
                if (f.exists()) {
                    url = f.toURI().toURL();
                }
            } else {
                File f = new File(path);
                if (f.exists()) {
                    url = f.toURI().toURL();
                }
            }
            if (url == null) {
                StreamlineMOTD.getInstance().logWarning("Invalid favicon path: " + path);
                return null;
            }
            return CosmicFavicon.createFromURL(url);
        } catch (Exception e) {
            StreamlineMOTD.getInstance().logWarning(e.getStackTrace());
            return null;
        }
    }

    public void setSample(String sample, int maxPlayers, int onlinePlayers) {
        String sampled = sample.replace("%newline%", "\n");
        String[] split = sampled.split("\n");

        PingedResponse.Players players = new PingedResponse.Players(maxPlayers, onlinePlayers, new PingedResponse.PlayerInfo[split.length]);

        for (int i = 0; i < split.length; i++) {
            players.getSample()[i] = new PingedResponse.PlayerInfo(ModuleUtils.codedString(ModuleUtils.replacePlaceholders(split[i])), UUID.randomUUID());
        }

        setSample(players);
    }

    public List<String> getMotdList() {
        reloadResource();

        return getResource().getOrSetDefault("motd.list", List.of("&7Welcome to &b&lMyServer&7!%newline%&7Join &4&lNOW&7.",
                "&7We have &b&l%motd_online%&7/&b&l%motd_max% &7players online!%newline%&7Join &4&lNOW&7."));
    }

    public SelectionType getMotdSelection() {
        reloadResource();

        try {
            return SelectionType.valueOf(getResource().getOrSetDefault("motd.selection", "RANDOM").toUpperCase());
        } catch (Exception e) {
            StreamlineMOTD.getInstance().logDebug(e.getStackTrace());
            return SelectionType.RANDOM;
        }
    }

    public int getMotdTicks() {
        reloadResource();

        return getResource().getOrSetDefault("motd.ticks", 20);
    }

    public int adjustPlayers(int real, String from) {
        String adjust = ModuleUtils.replacePlaceholders(from);

        try {
            return Integer.parseInt(adjust);
        } catch (Exception e) {
            try {
                return Math.round((float) MathUtils.eval(adjust));
            } catch (Exception ex) {
                StreamlineMOTD.getInstance().logDebug(ex.getStackTrace());
                return real;
            }
        }
    }

    public int getMaxPlayers() {
        String r = maxPlayers();

        return adjustPlayers(SLAPI.getInstance().getPlatform().getMaxPlayers(), r);
    }

    public int getOnlinePlayers() {
        String r = onlinePlayers();

        return adjustPlayers(SLAPI.getInstance().getPlatform().getOnlinePlayers().size(), r);
    }

    public List<String> getSampleList() {
        reloadResource();

        return getResource().getOrSetDefault("sample.list", List.of("&9Website&7: &bhttps://example.com%newline%&9Discord&7: &bhttps://discord.gg/example",
                "&9Website&7: &bhttps://example.com%newline%&9Discord&7: &bhttps://discord.gg/example%newline%&9Store&7: &bhttps://example.com/store"));
    }

    public SelectionType getSampleSelection() {
        reloadResource();

        try {
            return SelectionType.valueOf(getResource().getOrSetDefault("sample.selection", "RANDOM").toUpperCase());
        } catch (Exception e) {
            StreamlineMOTD.getInstance().logDebug(e.getStackTrace());
            return SelectionType.RANDOM;
        }
    }

    public int getSampleTicks() {
        reloadResource();

        return getResource().getOrSetDefault("sample.ticks", 20);
    }

    public String maxPlayers() {
        reloadResource();

        return getResource().getOrSetDefault("online.max", "100");
    }

    public String onlinePlayers() {
        reloadResource();

        return getResource().getOrSetDefault("online.current", "%streamline_players_online%");
    }

    public String versionName() {
        reloadResource();

        return getResource().getOrSetDefault("version.name", "1.20.1");
    }

    public String versionProtocol() {
        reloadResource();

        return getResource().getOrSetDefault("version.protocol", "");
    }

    public String getVersionProtocol() {
        String version = versionProtocol();

        try {
            version = ModuleUtils.replacePlaceholders(version);
            return String.valueOf(Integer.parseInt(version));
        } catch (Exception e) {
            return null;
        }
    }

    public UpdateType getUpdateType() {
        reloadResource();

        try {
            return UpdateType.valueOf(getResource().getOrSetDefault("update.type", UpdateType.ON_TICK.name()).toUpperCase());
        } catch (Exception e) {
            StreamlineMOTD.getInstance().logDebug(e.getStackTrace());
            return UpdateType.ON_TICK;
        }
    }

    public boolean isDescriptionEnabled() {
        reloadResource();

        return getResource().getOrSetDefault("motd.enabled", true);
    }

    public boolean isSampleEnabled() {
        reloadResource();

        return getResource().getOrSetDefault("sample.enabled", true);
    }

    public boolean isVersionEnabled() {
        reloadResource();

        return getResource().getOrSetDefault("version.enabled", true);
    }

    private boolean isFaviconEnabled() {
        reloadResource();

        return getResource().getOrSetDefault("favicon.enabled", true);
    }

    public String getFaviconFilePath() {
        reloadResource();

        return getResource().getOrSetDefault("favicon.path", "default");
    }
}
