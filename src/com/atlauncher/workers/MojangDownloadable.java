/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.workers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import com.atlauncher.App;
import com.atlauncher.data.Download;
import com.atlauncher.gui.Utils;

public class MojangDownloadable implements Runnable {

    private String url;
    private File file;
    private String md5;
    private HttpURLConnection connection;
    private InstanceInstaller instanceInstaller;
    private boolean restarted = false;

    public MojangDownloadable(String url, File file, String md5, InstanceInstaller instanceInstaller) {
        this.url = url;
        this.file = file;
        this.md5 = md5;
        this.instanceInstaller = instanceInstaller;
    }

    public MojangDownloadable(String url, File file, String md5) {
        this(url, file, md5, null);
    }

    public MojangDownloadable(String url, File file) {
        this(url, file, null, null);
    }

    /**
     * Gets the redirected URL
     * 
     * @param url
     *            URL to check for redirections
     * @return The redirected URL
     * @throws IOException
     */
    public URL getRedirect(String url) throws IOException {
        boolean redir;
        int redirects = 0;
        InputStream in = null;
        URL target = null;
        URL downloadURL = new URL(url);
        URLConnection c = downloadURL.openConnection();
        do {
            c.setRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.72 Safari/537.36");
            c.setConnectTimeout(5000);
            if (c instanceof HttpURLConnection) {
                ((HttpURLConnection) c).setInstanceFollowRedirects(false);
            }
            in = c.getInputStream();
            redir = false;
            if (c instanceof HttpURLConnection) {
                HttpURLConnection http = (HttpURLConnection) c;
                int stat = http.getResponseCode();
                if (stat >= 300 && stat <= 307 && stat != 306
                        && stat != HttpURLConnection.HTTP_NOT_MODIFIED) {
                    URL base = http.getURL();
                    String loc = http.getHeaderField("Location");
                    target = null;
                    if (loc != null) {
                        target = new URL(base, loc);
                    }
                    http.disconnect();
                    if (target == null
                            || !(target.getProtocol().equals("http") || target.getProtocol()
                                    .equals("https")) || redirects >= 5) {
                        throw new SecurityException("illegal URL redirect");
                    }
                    redir = true;
                    c = target.openConnection();
                    redirects++;
                }
            }
        } while (redir);
        if (target == null) {
            return downloadURL;
        } else {
            return target;
        }
    }

    public String getEtagFromURL(String url) {
        try {
            this.connection = (HttpURLConnection) new URL(url).openConnection();
            this.connection.setUseCaches(false);
            this.connection.setDefaultUseCaches(false);
            this.connection
                    .setRequestProperty(
                            "User-Agent",
                            "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.72 Safari/537.36");
            this.connection.setConnectTimeout(5000);
            this.connection.setRequestProperty("Cache-Control", "no-store,max-age=0,no-cache");
            this.connection.setRequestProperty("Expires", "0");
            this.connection.setRequestProperty("Pragma", "no-cache");
            this.connection.connect();
        } catch (MalformedURLException e) {
            App.settings.getConsole().logStackTrace(e);
        } catch (IOException e) {
            App.settings.getConsole().logStackTrace(e);
        }
        String etag = this.connection.getHeaderField("ETag");
        if (etag == null) {
            etag = "-";
        } else if ((etag.startsWith("\"")) && (etag.endsWith("\""))) {
            etag = etag.substring(1, etag.length() - 1);
        }
        return etag;
    }

    public String getMD5FromURL(String url) {
        try {
            this.connection = (HttpURLConnection) new URL(url).openConnection();
            this.connection.setUseCaches(false);
            this.connection.setDefaultUseCaches(false);
            this.connection
                    .setRequestProperty(
                            "User-Agent",
                            "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.72 Safari/537.36");
            this.connection.setConnectTimeout(5000);
            this.connection.setRequestProperty("Cache-Control", "no-store,max-age=0,no-cache");
            this.connection.setRequestProperty("Expires", "0");
            this.connection.setRequestProperty("Pragma", "no-cache");
            this.connection.connect();
        } catch (MalformedURLException e) {
            App.settings.getConsole().logStackTrace(e);
        } catch (IOException e) {
            App.settings.getConsole().logStackTrace(e);
        }
        String etag = this.connection.getHeaderField("ATLauncher-MD5");
        if (etag == null) {
            etag = "-";
        } else if ((etag.startsWith("\"")) && (etag.endsWith("\""))) {
            etag = etag.substring(1, etag.length() - 1);
        }
        return etag;
    }

    public void downloadFile() {
        try {
            InputStream in = null;
            URL downloadURL = getRedirect(this.url);
            if (this.connection == null) {
                this.connection = (HttpURLConnection) downloadURL.openConnection();
                this.connection
                        .setRequestProperty(
                                "User-Agent",
                                "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.72 Safari/537.36");
                this.connection.setConnectTimeout(5000);
            }
            in = this.connection.getInputStream();
            FileOutputStream writer = new FileOutputStream(this.file);
            byte[] buffer = new byte[1024];
            int bytesRead = 0;
            while ((bytesRead = in.read(buffer)) > 0) {
                writer.write(buffer, 0, bytesRead);
                buffer = new byte[1024];
            }
            writer.close();
            in.close();
        } catch (FileNotFoundException e) {
            if (!restarted) {
                if (file.getName().equalsIgnoreCase("scala-library-2.10.2.jar")) {
                    this.restarted = true;
                    this.connection = null;
                    this.url = App.settings.getFileURL("forge/scala-library-2.10.2.jar");
                    this.md5 = getMD5FromURL(this.url);
                } else if (file.getName().equalsIgnoreCase("scala-compiler-2.10.2.jar")) {
                    this.restarted = true;
                    this.connection = null;
                    this.url = App.settings.getFileURL("forge/scala-compiler-2.10.2.jar");
                    this.md5 = getMD5FromURL(this.url);
                }
                if (!restarted) {
                    App.settings.getConsole().logStackTrace(e);
                    instanceInstaller.cancel(true);
                }
            } else {
                App.settings.getConsole().logStackTrace(e);
                instanceInstaller.cancel(true);
            }
        } catch (IOException e) {
            App.settings.getConsole().logStackTrace(e);
        }
    }

    @Override
    public void run() {
        if (instanceInstaller.isCancelled()) {
            return;
        }
        instanceInstaller.setDoingResources("Downloading " + file.getName());
        if (this.md5 == null) {
            this.md5 = getEtagFromURL(this.url);
        }
        // Create the directory structure
        new File(file.getAbsolutePath().substring(0,
                file.getAbsolutePath().lastIndexOf(File.separatorChar))).mkdirs();
        if (this.md5.equalsIgnoreCase("-")) {
            downloadFile(); // Only download the file once since we have no MD5 to check
        } else {
            int tries = 0;
            while (!Utils.getMD5(this.file).equalsIgnoreCase(this.md5) && tries <= 3) {
                tries++;
                downloadFile(); // Keep downloading file until it matches MD5, up to 3 times
            }
        }
    }
}
