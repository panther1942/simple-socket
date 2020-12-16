package cn.erika.aop.scan;

import cn.erika.config.Constant;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
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

    private static final String CHARSET = "UTF-8";
    private static PackageScanner scanner;
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
    public void scan() {
        for (String packageName : packageList) {
            scan(packageName);
        }
    }

    // 开始扫描
    // 基本原理就是读取class文件 分析其路径名
    private void scan(String packageName) {
        String systemName = System.getProperty("os.name");
        if (systemName == null) {
            System.err.println("无法获取操作系统类型");
            System.exit(1);
        }
        System.out.println(systemName);
        String dirName = null;
        if (systemName.startsWith(Constant.LINUX)) {
            dirName = packageName.replaceAll("\\.", File.separator);
        } else if (systemName.startsWith(Constant.WINDOWS)) {
            dirName = packageName.replaceAll("\\.", Matcher.quoteReplacement(File.separator));
        } else {
            System.err.println("不支持的操作系统");
            System.exit(1);
        }
        Enumeration<URL> dirs = null;
        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(dirName);
            while (dirs.hasMoreElements()) {
                URL url = dirs.nextElement();
                switch (url.getProtocol()) {
                    case "file":
                        String filePath = URLDecoder.decode(url.getFile(), CHARSET);
                        getClass(packageName, filePath);
                        break;
                    case "jar":
                        URLConnection conn = url.openConnection();
                        JarFile jar = ((JarURLConnection) conn).getJarFile();
                        getClass(jar, packageName);
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 使用处理器处理扫描到的类文件
    private void onScan(Class<?> clazz) {
        for (PackageScannerHandler handler : this.handlerList) {
            if (handler.filter(clazz)) {
                handler.deal(clazz);
            }
        }
    }

    // 如果扫描到的是class文件
    private void getClass(String packageName, String packagePath) {
        File dir = new File(packagePath);
        if (!dir.exists() || !dir.isDirectory()) {
            System.err.println("在包名:" + packageName + "下未找到类文件");
        } else {
            File[] files = dir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return (file.isDirectory() || file.getName().endsWith(".class"));
                }
            });
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        getClass(
                                packageName + "." + file.getName(),
                                file.getAbsolutePath());
                    } else {
                        String className = file.getName().substring(0, file.getName().length() - 6);
                        try {
                            Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(packageName + "." + className);
                            onScan(clazz);
                        } catch (ClassNotFoundException e) {
                            System.err.println("找不到类: " + e.getException());
                        }
                    }
                }
            } else {
                System.err.println("在包名:" + packageName + "下未找到类文件");
            }
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
            if (!entry.isDirectory() && entryName.endsWith(".class")) {
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
                    Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(packageName + "." + className);
                    onScan(clazz);
                } catch (ClassNotFoundException e) {
                    System.err.println("找不到类: " + e.getException());
                }
            }
        }
    }
}
