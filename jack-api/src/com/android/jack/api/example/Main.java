package com.android.jack.api.example;


import com.android.jack.api.AbortException;
import com.android.jack.api.JackConfig;
import com.android.jack.api.JackConfigProvider;
import com.android.jack.api.UnrecoverableException;
import com.android.jack.api.arzon.ArzonCompiler;
import com.android.jack.api.arzon.ArzonConfig;
import com.android.jack.api.brest.BrestConfig;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 *
 */
public class Main {

  /**
   *
   * @param args
   * @throws MalformedURLException
   * @throws ClassNotFoundException
   * @throws SecurityException
   * @throws NoSuchMethodException
   * @throws IllegalArgumentException
   * @throws InstantiationException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  public static void main(String[] args) throws MalformedURLException, ClassNotFoundException,
      SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException,
      IllegalAccessException, InvocationTargetException {
    ClassLoader loader =
        URLClassLoader.newInstance(new URL[] {new File(
            "/Users/jplesot/Android/ub-jack/toolchain/jack/jack/dist/jack.jar").toURI().toURL()},
            Main.class.getClassLoader());

    Class<? extends JackConfigProvider> confProviderClass =
        Class.forName(JackConfigProvider.CLASS_NAME, true, loader).asSubclass(
            JackConfigProvider.class);

    JackConfigProvider confProvider = confProviderClass.getConstructor().newInstance();

    System.out.println("Jack version: " + confProvider.getCompilerVersion() + " '"
        + confProvider.getCompilerCodeName() + "' (" + confProvider.getCompilerBuildId() + " "
        + confProvider.getCompilerCodeBase() + ")");

    System.out.println("Supported configs: ");
    for (Class<? extends JackConfig> cls : confProvider.getSupportedConfigs()) {
      System.out.print(cls.getSimpleName() + " ");
    }
    System.out.println();

    ArzonConfig arzonConfig = confProvider.getConfig(ArzonConfig.class);
    arzonConfig.setProperty("c.a.n.arzon", "bar").setProperty("a.b.c.d", "foo");
    ArzonCompiler arzonCompiler = arzonConfig.build();
    try {
      arzonCompiler.run();
    } catch (AbortException e) {
      e.printStackTrace();
    } catch (UnrecoverableException e) {
      e.printStackTrace();
    }

    BrestConfig brest = confProvider.getConfig(BrestConfig.class);
    brest.setProperty("c.a.n.brest", "toto");
    try {
      brest.build().run();
    } catch (AbortException e) {
      e.printStackTrace();
    } catch (UnrecoverableException e) {
      e.printStackTrace();
    }
  }
}
