package update;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
 
//javaw -cp "autoUpdate_v1.0.jar" update.AutoUpdateThread "http://localhost/autoUpdate_v{ver}.jar"

public class AutoUpdateJob extends TimerTask {  
	
    public static void main(String[] args) { 
    	assertArgs(args);
        try {  
        	
        	AutoUpdateJob job = new AutoUpdateJob();
        	job.setUpdateUrl(args[0]);
        	job.start(0, 1000 * 10);
        	
    		
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
    }  

    public AutoUpdateJob() {
    }
    
	public AutoUpdateJob(String[] args) {
		this.updateUrl = args[0];
		this.parseUpdateUrl();
	}
  
    @Override  
    public void run() {  
    	isActiveChecked();
        String version = getVerCur();
        String basepath = getBasePath();  
        String downpath = downbase;
       
        try {  
        	if(checkVerPlus()){
        		System.out.println("I am updating, version: "+ version+" -> "+getVerPlus()+"\r\n");
        		String verPlusName = filename.replace(VAR, getVerPlus());
        		String verCurrName = filename.replace(VAR, getVerCur());
        		downloadJar(downpath+verPlusName, basepath+verPlusName);
        		replaceShell(verCurrName, verPlusName);
        		deleteOldJar(basepath+verCurrName);
        		restartJar(basepath+verPlusName);
        	}else{
        		String verLowName = filename.replace(VAR, getVerLow());
        		deleteOldJar(basepath+verLowName);
        		
        		System.out.println("No update, I am working, version: "+ version+"\r\n");
        	}
        } catch (Exception e) {   
            e.printStackTrace();  
        }
    }  
    
    public String getVerCur() {
		Properties prop = System.getProperties();
		String regex = filename.replace(VAR, "(.*?)");
		Pattern p = Pattern.compile(regex);
		String s = String.valueOf(prop);
		Matcher m = p.matcher(s);
		while (m.find()) {
			String ver = m.group(1);
			if(VAR.equals(ver)){
				continue;
			}else{
				return ver;
			}
		}
		return "";
	}
    
    public String getBasePath(){
    	return new File("").getAbsolutePath()+File.separator;
    }
    
    public String getVerPlus(){
    	String version = getVerCur();
    	System.out.println("current vesion is : "+version);
    	Double verPlus = Double.valueOf(version) + 1.0;
    	String verPlus1 = new DecimalFormat("0.0").format(verPlus);
    	return verPlus1;
    }
    
    protected String getVerLow(){
    	String version = getVerCur();
    	Double verLow = Double.valueOf(version) - 1.0;
    	String verLow1 = new DecimalFormat("0.0").format(verLow);
    	return verLow1;
    }
    
    protected boolean checkVerPlus(){
    	String verPlus = filename.replace(VAR, getVerPlus());
    	System.out.println("check new vesion : " + verPlus);
    	return checkJar(downbase+verPlus);
    }
  
    protected void restartJar(String filename) {
        try {  
            String javaCommand = System.getProperty("sun.java.command");
            String execLine = "java -cp \"" + filename + "\" "+javaCommand;
            System.out.println(execLine);

            execCmd(execLine);
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
    }  
    
    protected void execCmd(String cmdLine){
        Runtime runtime = Runtime.getRuntime();
        Process p=null;
        try {  
             p = runtime.exec(cmdLine, null, null);         
             final InputStream is1 = p.getInputStream();   
             final InputStream is2 = p.getErrorStream();  
             new Thread() {  
                public void run() {  
                   BufferedReader br1 = new BufferedReader(new InputStreamReader(is1));  
                    try {  
                        String line1 = null;  
                        while ((line1 = br1.readLine()) != null) {  
                              if (line1 != null){
                                  System.out.println(line1);
                              }  
                          }  
                    } catch (IOException e) {  
                         e.printStackTrace();  
                    }  
                    finally{  
                         try {  
                           is1.close();  
                         } catch (IOException e) {  
                            e.printStackTrace();  
                        }  
                    }  
                }  
            }.start();  

            new Thread() {
                  public void  run() {
                   BufferedReader br2 = new  BufferedReader(new  InputStreamReader(is2));   
                      try {   
                         String line2 = null ;   
                         while ((line2 = br2.readLine()) !=  null ) {   
                              if (line2 != null){
                            	  System.out.println(line2);
                              }  
                         }   
                       } catch (IOException e) {   
                             e.printStackTrace();  
                       }   
                      finally{  
                         try {  
                             is2.close();  
                         } catch (IOException e) {  
                             e.printStackTrace();  
                         }  
                       }  
                    }   
             }.start();     
                  shutdown = true;
                  isActiveChecked();
                  
                  p.waitFor();  
                  p.destroy();   
         } catch (Exception e) {
			try {
				  p.getErrorStream().close();
				  p.getInputStream().close();
				  p.getOutputStream().close();
			} catch (Exception ee) {
			} 
        } 
    }
    
    protected boolean checkJar(String url){
		try{
			URL target = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) target.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(30000);
			conn.connect();
			int code = conn.getResponseCode();
			boolean success = (code >= 200) && (code < 300);
			conn.disconnect();
			return success;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
  
	protected void downloadJar(String url, String filename) throws Exception {  
        URL target = new URL(url);  
        HttpURLConnection conn = (HttpURLConnection) target.openConnection();  
        conn.setDoOutput(true);  
        conn.setDoInput(true);  
        conn.setRequestMethod("GET");  
        conn.setConnectTimeout(30000);  
        conn.setReadTimeout(30000);  
        conn.connect();  
        int code = conn.getResponseCode();  
        boolean success = (code >= 200) && (code < 300);  
        byte[] buffer = null;  
        if (success) {  
            InputStream in = conn.getInputStream();  
            int size = conn.getContentLength();   
            buffer = new byte[size];  
            int curr = 0, read = 0;  
            while (curr < size) {  
                read = in.read(buffer, curr, size - curr);  
                if (read <= 0) {  
                    break;  
                }  
                curr += read;  
            }   
            in.close();  
        }  
        File newfile = new File(filename); 
        OutputStream ops = new BufferedOutputStream(new FileOutputStream(newfile,false));  
        ops.write(buffer);  
        ops.close();    
        conn.disconnect();
    }  
    
    protected void replaceShell(String oldVer, String newVer) throws Exception { 
    	File dir = new File(new File("").getAbsolutePath()); 
    	File[] files = dir.listFiles(new FileFilter (){
			public boolean accept(File arg0) {
				if(!arg0.isFile()) return false;
				String filename = arg0.getName().toLowerCase();
				return filename.endsWith(".bat") || filename.endsWith(".sh");
			}
    	});
		if (files != null){
			for (File file : files) {
				replaceShell(file, oldVer, newVer);
			}
		}
    }
    
    protected void replaceShell(File file, String oldVer, String newVer) throws Exception { 
    	String content = readFile(file);
    	if(content==null) return;
    	content = content.replace(oldVer, newVer);
    	content = content.replace(oldVer, newVer);
    	writeFile(file, content);
    }
    
    protected void writeFile(File filePath, String content) throws IOException {
		FileOutputStream out = new FileOutputStream(filePath, false);
		out.write(content.getBytes());
		out.close();
	}
	
	protected String readFile(File file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		String content = null;
		StringBuilder sb = new StringBuilder();
		content = br.readLine();
		while (content != null) {
			sb.append(content+"\r\n");
			content = br.readLine();
		}
		br.close();
		return sb.toString();
	}
    
	protected void deleteOldJar(String filePath){
		try{
			if(new File(filePath).exists()){
				new File(filePath).delete();
				new File(filePath).deleteOnExit();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
    public void start(long delay, long period){
    	if(updateUrl == null){
    		return;
    	}
		timer.schedule(this, delay, period);
        timer.schedule(new TimerTask(){
			public void run() {
				isActiveChecked();
			}
        }, 100, 100);
    }
    
    protected void isActiveChecked(){
    	if(shutdown){
    		System.out.println("Version: "+getVerCur()+" Exit");
    		timer.cancel();
    		System.exit(0);
		}
    }
    
    public String getUpdateUrl() {
		return updateUrl;
	}

	protected void parseUpdateUrl(){
		String url = this.updateUrl;
		downbase = url.substring(0, url.lastIndexOf("/") + 1);
		filename = url.substring(url.lastIndexOf("/") + 1);
		System.out.println("\r\nStart! "+downbase + filename);
	}
	
	public void setUpdateUrl(String updateUrl) {
		this.updateUrl = updateUrl;
		this.parseUpdateUrl();
	}
	
	public static void assertArgs(String[] args) {
		String url = "";
		if (args != null && args.length > 0) {
			if (args[0].indexOf("://") > 0) {
				url = args[0].trim();
			}
		}
		assert url == "" : "updageUrl is null";
	}

	private String updateUrl = null;
    private Boolean shutdown = false;
    private Timer timer = new Timer();
    private String VAR = "{ver}";
    private String filename, downbase;
}  