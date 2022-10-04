package bin.mt.plugin.packer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@SuppressWarnings("SdCardPath")
public class PluginPacker {
    /**
     * adb所在路径
     */
    public static final String ADB_PATH = "/Users/linjinbin/Library/Android/sdk/platform-tools/adb";

    /**
     * 打包完成后是否将插件安装包推送到手机
     */
    public static final boolean PUSH_MTP_TO_DEVICE = true;

    public static void main(String[] args) throws Exception {
        List<File> outList = new ArrayList<>();
        File rootDir = new File("").getAbsoluteFile();

        System.out.println("Packing... ");
        for (File file : Objects.requireNonNull(rootDir.listFiles())) {
            if (file.isDirectory() && file.getName().startsWith("translator-")) {
                outList.add(pack(rootDir, file.getName()));
            }
        }

        if (PUSH_MTP_TO_DEVICE) {
            System.out.println("Pushing...");
            for (File outFile : outList) {
                System.out.println(">> /sdcard/MT2/plugin/" + outFile.getName());
                String[] commands = {
                        ADB_PATH,
                        "push",
                        outFile.getAbsolutePath(),
                        "/sdcard/MT2/plugin/" + outFile.getName()
                };
                Process process = Runtime.getRuntime().exec(commands);
                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    System.err.println("Push failed.");
                    System.exit(exitCode);
                }
            }
        }
        System.out.println("Done.");
    }

    public static File pack(File rootDir, String moduleName) throws Exception {
        System.out.println(">> " + moduleName);
        File srcDir = new File(rootDir, moduleName + "/src/main/java");
        File commonSrcDir = new File(rootDir, "common/src/main/java");
        File gradleFile = new File(rootDir, moduleName + "/build.gradle");
        File assetsDir = new File(rootDir, moduleName + "/src/main/assets");
        File manifestFile = new File(rootDir, moduleName + "/src/main/resources/manifest.json");
        File iconFile1 = new File(moduleName + "/src/main/resources/icon.png");
        File iconFile2 = new File(moduleName + "/src/main/resources/icon.jpg");
        File libsDir = new File(rootDir, "libs");
        File outFile = new File(rootDir, "out/" + moduleName.substring("translator-".length()) + ".mtp");

        //noinspection ResultOfMethodCallIgnored
        outFile.getParentFile().mkdirs();

        String gradleText = new String(Files.readAllBytes(gradleFile.toPath()));

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outFile))) {
            zos.setLevel(Deflater.BEST_COMPRESSION);
            zos.setMethod(ZipOutputStream.DEFLATED);
            addDirectory(zos, srcDir, "src/", true);
            if (gradleText.contains("implementation project(':common')")) {
                addDirectory(zos, commonSrcDir, "src/", true);
            }
            addDirectory(zos, assetsDir, "assets/", false);
            if (gradleText.contains("implementation fileTree(dir: '../libs', include: ['*.jar'])") && libsDir.isDirectory()) {
                File[] files = libsDir.listFiles((pathname) -> {
                    if (!pathname.isFile()) {
                        return false;
                    } else {
                        String name = pathname.getName().toLowerCase();
                        return name.endsWith(".jar") && !name.endsWith("-sources.jar");
                    }
                });
                if (files != null) {
                    zos.setMethod(ZipOutputStream.STORED);
                    CRC32 crc32 = new CRC32();
                    for (File file : files) {
                        byte[] data = Files.readAllBytes(file.toPath());
                        crc32.reset();
                        crc32.update(data);
                        ZipEntry entry = new ZipEntry("libs/" + file.getName());
                        entry.setSize(file.length());
                        entry.setCrc(crc32.getValue());
                        zos.putNextEntry(entry);
                        zos.write(data);
                        zos.closeEntry();
                    }
                    zos.setMethod(ZipOutputStream.DEFLATED);
                }
            }
            zos.putNextEntry(new ZipEntry("manifest.json"));
            zos.write(Files.readAllBytes(manifestFile.toPath()));
            zos.closeEntry();
            if (iconFile1.isFile()) {
                zos.putNextEntry(new ZipEntry(iconFile1.getName()));
                zos.write(Files.readAllBytes(iconFile1.toPath()));
                zos.closeEntry();
            } else if (iconFile2.isFile()) {
                zos.putNextEntry(new ZipEntry(iconFile2.getName()));
                zos.write(Files.readAllBytes(iconFile2.toPath()));
                zos.closeEntry();
            }
        }
        return outFile;
    }

    private static void addDirectory(ZipOutputStream zos, File dir, String parentPathInZip, boolean onlyJava) throws IOException {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    addDirectory(zos, file, parentPathInZip + file.getName() + "/", onlyJava);
                } else {
                    String name = file.getName();
                    if (!name.equals(".DS_Store") && (!onlyJava || name.toLowerCase().endsWith(".java"))) {
                        addFile(zos, file, parentPathInZip);
                    }
                }
            }
        }
    }

    private static void addFile(ZipOutputStream zos, File file, String parentPathInZip) throws IOException {
        zos.putNextEntry(new ZipEntry(parentPathInZip + file.getName()));
        zos.write(Files.readAllBytes(file.toPath()));
        zos.closeEntry();
    }

}