package cise.io;

import cise.mergeinfo.MergedGraphInfo;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MergedGraphIO {
    public static class MergedGraphExporter {

        /**
         * 序列化一个融合方案。根据时间自动生成文件名
         * @param mgInfo
         * @param directory 存储的文件夹
         */
        public static void saveMergedGraph(MergedGraphInfo mgInfo, String directory) {

            Path saveDir = Paths.get(directory);
            File saveDirFile = saveDir.toFile();

            if (!saveDirFile.exists() || !saveDirFile.isDirectory()) {
                System.out.println("文件夹不存在，序列化失败");
                return;
            }

            try {
                String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss"));
                Path filePath = saveDir.resolve(time + ".out");

                // 测试用
                //Path filePath = saveDir.resolve("test.out");

                File saveFile = filePath.toFile();
                if(!saveFile.exists()) {
                    saveFile.createNewFile();
                }
                ObjectOutputStream objectOut = new ObjectOutputStream(new FileOutputStream(saveFile));
                objectOut.writeObject(mgInfo);
                objectOut.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class MergedGraphImporter {
        public static MergedGraphInfo loadMergedGraph(String path) {

            File loadFile = new File(path);
            if (!loadFile.exists() || !loadFile.isFile()) {
                System.out.println("文件不存在，反序列化失败");
                return null;
            }

            try {
                ObjectInputStream objectIn = new ObjectInputStream(new FileInputStream(loadFile));
                MergedGraphInfo mgInfo = (MergedGraphInfo) objectIn.readObject();
                objectIn.close();
                return mgInfo;

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

}
