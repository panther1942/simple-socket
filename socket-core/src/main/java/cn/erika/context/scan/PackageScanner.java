package cn.erika.context.scan;

import cn.erika.util.log.Logger;
import cn.erika.util.log.LoggerFactory;

import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;

// 包扫描器
// 注意!!! loadClass可能会导致提前触发依赖缺失的异常
// 扫包时注意适用范围
public class PackageScanner {
    private static final String LINUX = "Linux";
    private static final String WINDOWS = "Windows";
    private static final String FILE = "file";
    private static final String JAR = "jar";
    private static final String CLASS_SUFFIX = ".class";
    private static final String JAVA_OS_NAME = "os.name";
    private static final String CHARSET = System.getProperty("file.encoding");

    private static PackageScanner scanner;
    private Logger log = LoggerFactory.getLogger(this.getClass());
    // 要扫描的包名
    private List<String> packageList = new LinkedList<>();
    // 处理器列表
    private List<PackageScannerHandler> handlerList = new LinkedList<>();

    private PackageScanner() {
    }

    public static PackageScanner getInstance() {
        if (scanner == null) {
            scanner = new PackageScanner();
        }
        return scanner;
    }

    // 添加处理器
    public void addHandler(PackageScannerHandler handler) {
        this.handlerList.add(handler);
    }

    // 添加要扫描的包
    public void addPackage(String packageName) {
        if (packageName == null || !packageName.matches("[\\w]+(\\.[\\w]+)*")) {
            throw new IllegalArgumentException("包名非法: " + packageName);
        }
        this.packageList.add(packageName);
    }

    // 开始扫描
    public void scan() throws IOException {
        for (String packageName : packageList) {
            scan(packageName);
        }
    }

    /**
     * 扫描类文件的方法 通过加载类加载器获取项目根目录
     * 遍历该目录下的所有文件
     * 如果路径匹配包名 则
     *
     * @param packageName
     * @throws IOException
     */
    private void scan(String packageName) throws IOException {
        String dirName = convertClassName2Path(packageName);

        Enumeration<URL> dirs = Thread.currentThread().getContextClassLoader().getResources(dirName);
        while (dirs.hasMoreElements()) {
            URL url = dirs.nextElement();
            switch (url.getProtocol()) {
                case FILE:
                    String filePath = URLDecoder.decode(url.getFile(), CHARSET);
                    getClass(packageName, filePath);
                    break;
                case JAR:
                    URLConnection conn = url.openConnection();
                    JarFile jar = ((JarURLConnection) conn).getJarFile();
                    getClass(jar, packageName);
                    break;
                default:
                    continue;
            }
        }
    }

    // 使用处理器处理扫描到的类文件
    // 如果扫描到的是class文件
    private void getClass(String packageName, String packagePath) {
        File dir = new File(packagePath);
        if (!dir.exists() || !dir.isDirectory()) {
            log.warn("在包名:" + packageName + "下未找到类文件");
            return;
        }
        File[] files = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return (file.isDirectory() || file.getName().endsWith(CLASS_SUFFIX));
            }
        });
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    getClass(packageName + "." + file.getName(), file.getAbsolutePath());
                } else {
                    String className = file.getName().substring(0, file.getName().length() - 6);
                    try {
                        Class<?> clazz = Class.forName(packageName + "." + className);
                        onScan(clazz);
                    } catch (ClassNotFoundException e) {
                        System.err.println("找不到类: " + e.getMessage());
                    }
                }
            }
        } else {
            System.err.println("在包名:" + packageName + "下未找到类文件");
        }
    }

    // 如果扫描到的是jar包
    private void getClass(JarFile jar, String packagePath) {
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String entryName = entry.getName();
            if (!entryName.startsWith(packagePath.replaceAll("\\.", File.separator))) {
                continue;
            }
            if (entryName.startsWith("/")) {
                entryName = entryName.substring(1);
            }
            if (!entry.isDirectory() && entryName.endsWith(CLASS_SUFFIX)) {
                String packageName = null;
                int idx = entryName.lastIndexOf("/");
                if (idx != -1) {
                    packageName = entryName.substring(0, idx).replaceAll(File.separator, ".");
                }
                String className = entryName.substring(
                        packageName == null ? 0 : packageName.length() + 1,
                        entryName.length() - 6
                );
                try {
                    Class<?> clazz = Class.forName(packageName + "." + className);
                    onScan(clazz);
                } catch (ClassNotFoundException e) {
                    System.err.println("找不到类: " + e.getException());
                }
            }
        }
    }

    private void onScan(Class<?> clazz) {
//        System.out.println("加载类: " + clazz.getName());
        for (PackageScannerHandler handler : this.handlerList) {
            if (handler.filter(clazz)) {
                handler.deal(clazz);
            }
        }
    }

    public static String convertClassName2Path(String packageName) {
        // 需要判断操作系统类型 因为不同系统的分隔符不一样
        String systemName = System.getProperty(JAVA_OS_NAME);
        if (systemName == null) {
            throw new RuntimeException("无法获取操作系统类型");
        }
        if (systemName.startsWith(LINUX)) {
            // Linux下的分隔符是 / 无需处理
            return packageName.replaceAll("\\.", File.separator);
        } else if (systemName.startsWith(WINDOWS)) {
            // Windows下的分隔符是 \ 在Java中这是转义字符 因此需要处理
            return packageName.replaceAll("\\.", Matcher.quoteReplacement(File.separator));
        } else {
            // 其他类型的操作系统我也没用过
            throw new RuntimeException("不支持的操作系统");
        }
    }

    public static String convertPath2ClassName(String path) {
        // 需要判断操作系统类型 因为不同系统的分隔符不一样
        String systemName = System.getProperty(JAVA_OS_NAME);
        if (systemName == null) {
            throw new RuntimeException("无法获取操作系统类型");
        }
        if (systemName.startsWith(LINUX)) {
            // Linux下的分隔符是 / 无需处理
            return path.replaceAll(File.separator, "\\.");
        } else if (systemName.startsWith(WINDOWS)) {
            // Windows下的分隔符是 \ 在Java中这是转义字符 因此需要处理
            return path.replaceAll(Matcher.quoteReplacement(File.separator), "\\.");
        } else {
            // 其他类型的操作系统我也没用过
            throw new RuntimeException("不支持的操作系统");
        }
    }
}
