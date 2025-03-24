package sk.breezelook.config;

import com.google.gson.annotations.SerializedName;
import net.fabricmc.loader.impl.FabricLoaderImpl;

import java.io.File;
import org.apache.logging.log4j.Logger;
import sk.breezelook.FileHelper;
import sk.breezelook.Main;

public class CommonConfig extends AbstractConfig {

    public CommonConfigModel model = new CommonConfigModel();

    public CommonConfig() {
        super(new File(FabricLoaderImpl.INSTANCE.getConfigDir().toFile().getAbsolutePath() , "breezelook/config.json").getAbsolutePath());
        readConfig();
        checkConfig();
    }

    public void checkConfig() {
        writeConfig();
    }

    public void writeConfig() {
        try {
            createFile();
            FileHelper.write(getConfigFile(), AbstractConfig.GSON.toJson(model));
        }
        catch (Exception e){
            Main.LOGGER.error("failed to write config", e);
        }
    }

    public void readConfig() {
        try {
            CommonConfigModel model = AbstractConfig.GSON.fromJson(FileHelper.read(getConfigFile()), CommonConfigModel.class);
            if (model != null) this.model = model;
            else throw new NullPointerException();
        }
        catch (Exception e){
            Main.LOGGER.error("failed to read config", e);
        }
    }

    public static class CommonConfigModel {
        @SerializedName("confirm-delay-tick")
        public int confirmDelay = def_confirmDelay;
        @SerializedName("confirm-horizontal")
        public float confirmHorizontal = def_confirmHorizontal;
        @SerializedName("confirm-vertical")
        public float confirmVertical = def_confirmVertical;
        @SerializedName("return-camera")
        public boolean returnCamera = def_returnCamera;
        @SerializedName("return-camera-delay-tick")
        public int returnCameraDelay = def_returnCameraDelay;
        @SerializedName("return-camera-to-direction")
        public boolean returnCameraToDirection = def_returnCameraToDirection;
        @SerializedName("return-camera-to-horizontal")
        public float returnCameraToHorizontal = def_returnCameraToHorizontal;
        @SerializedName("return-camera-to-vertical")
        public float returnCameraToVertical = def_returnCameraToVertical;

        public static final int def_confirmDelay = 60;
        public static final float def_confirmHorizontal = 0;
        public static final float def_confirmVertical = -90;
        public static final boolean def_returnCamera = false;
        public static final int def_returnCameraDelay = 20;
        public static final boolean def_returnCameraToDirection = false;
        public static final float def_returnCameraToHorizontal = 0;
        public static final float def_returnCameraToVertical = 0;
    }
}
