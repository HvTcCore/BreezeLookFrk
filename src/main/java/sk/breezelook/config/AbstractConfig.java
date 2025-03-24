package sk.breezelook.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractConfig {
    public static Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public String Path;
    public AbstractConfig(String Path) {
        this.Path = Path;
        getDir(new File(Path)).mkdirs();
    }
    public abstract void checkConfig();
    public abstract void writeConfig();
    public abstract void readConfig();
    public boolean createFile() throws IOException {
        return new File(Path).createNewFile();
    }
    public File getConfigFile() {
        return new File(Path);
    }
    public static File getDir(File src) {
        String AbsolutePath = src.getAbsolutePath();
        AbsolutePath = AbsolutePath.replace("\\","/");
        List<String> StringList = new ArrayList<>(List.of(AbsolutePath.split("/")));
        StringList.remove(StringList.get(StringList.size() - 1));
        return new File(String.join("/", StringList));
    }
}