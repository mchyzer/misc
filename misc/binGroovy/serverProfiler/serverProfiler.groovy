#!/bin/env groovy

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.*

//if a process uses more than 5% of memory or cpu, then call it out
int thresholdTimesTen = 50;

if (this.args.size() == 1) {
  thresholdTimesTen = 10 * Integer.parseInt(this.args[0]);
}



def proc = ("/usr/bin/sudo /usr/bin/top -bn 2 -d 0.01").execute();

proc.waitFor();

def topOutput;
if (proc.exitValue() == 0) {
  topOutput = proc.in.text;
} else {
  throw new RuntimeException("Exit code from top: " + proc.exitValue());
}

//System.out.println(topOutput);

String[] topLines = topOutput.split("\n");

//first line is load average
// top - 15:10:24 up 170 days, 18:05,  2 users,  load average: 0.01, 0.03, 0.00

Pattern pattern = Pattern.compile('^top.*load average: (.*), (.*), (.*).*$');

int line = 8;

Matcher matcher = null;

boolean foundIt = false;
for (line = 8; line<topLines.length;line++) {
  matcher = pattern.matcher(topLines[line]);

  if (matcher.matches()) {
    foundIt = true;
    break;
  }
}

if (!foundIt) {

  throw new RuntimeException("Cant find second top line");

}
String load1min = matcher.group(1);
String load5min = matcher.group(2);
String load15min = matcher.group(3);

//third line is cpu
// Cpu(s): 51.0%us,  0.3%sy,  0.0%ni,  0.0%id,  0.0%wa,  0.0%hi,  0.0%si, 48.7%st

pattern = Pattern.compile('^Cpu\\(s\\):.*[,\\s](.*)%id.*$');

matcher = pattern.matcher(topLines[line+2]);

if (!matcher.matches()) {
  throw new RuntimeException("Cant match pattern for: " + topLines[line+2]);
}

String cpu = "" + (100.0 - Double.parseDouble(matcher.group(1)));

//4th line is memory
// Mem:   1918456k total,  1849892k used,    68564k free,   391600k buffers

pattern = Pattern.compile('^Mem:\\s*(.*) total,\\s+(.*) used().*$');

matcher = pattern.matcher(topLines[line+3]);

if (!matcher.matches()) {
  throw new RuntimeException("Cant match pattern for: " + topLines[line+3]);
}

String memoryTotal = matcher.group(1);
String memoryUsed = matcher.group(2);

pattern = Pattern.compile('^\\s*PID.*$');

matcher = pattern.matcher(topLines[line+6]);

if (!matcher.matches()) {
  throw new RuntimeException("Cant match pattern for: " + topLines[line+6]);
}

int httpdProcessCount = 0;
long httpdMemory = 0;
int httpdPercentMemoryTen = 0;
int httpdPercentCpuTen = 0;

int javaProcessCount = 0
long javaMemory = 0;
int javaPercentMemoryTen = 0;
int javaPercentCpuTen = 0;


// 8th line is where processes start
for (int i=line+7; i<topLines.length; i++) {

  String topLine = topLines[i].trim();
  if (topLine == null || "".equals(topLine.trim())) {
  
    continue;

  }

  //  1599 appadmin  20   0  801m 624m 8260 S 99.8 37.7  38:37.28 java
  pattern = Pattern.compile('^\\s*(\\S+)\\s+\\S+\\s+\\S+\\s+\\S+\\s+\\S+\\s+(\\S+)\\s+\\S+\\s+\\S+\\s+(\\S+)\\s+(\\S+)\\s+\\S+\\s+(.*)\\s*$');      

  matcher = pattern.matcher(topLine);

  if (!matcher.matches()) {
    
    throw new RuntimeException("Cant match pattern for: " + i + ", " + topLine);

  }

  String pid = matcher.group(1);
  String memory = matcher.group(2);
  String cpuPercent = matcher.group(3);
  String memoryPercent = matcher.group(4);
  String command = matcher.group(5);

  String commandFriendly = command;

  int cpuPercentTimesTen =  (int)(Double.parseDouble(cpuPercent) * 10);
  int memoryPercentTimesTen = (int)(Double.parseDouble(memoryPercent) * 10);

  long memoryBytes = convertMemory(memory);

  boolean isHttpd = false;
  boolean isJava = false;

  if (command != null && command.startsWith("httpd")) {
    httpdProcessCount++;
    httpdMemory += memoryBytes;
    httpdPercentCpuTen += cpuPercentTimesTen;
    httpdPercentMemoryTen += memoryPercentTimesTen;

    isHttpd = true;

  } else if ("java".equals(command)) {
    
    javaProcessCount++;
    javaMemory += memoryBytes;
    javaPercentCpuTen += cpuPercentTimesTen;
    javaPercentMemoryTen += memoryPercentTimesTen;

    isJava = true;

  }

  if (cpuPercentTimesTen > thresholdTimesTen || memoryPercentTimesTen > thresholdTimesTen) {

    //appadmin  3360     1  0 Jul24 ?        00:13:10 /opt/appserv/tomcat/apps/fastPdfService/java/bin/java -server -Xms5M -Xmx90M -XX:MaxPermSize=100M -Dfile.encoding=UTF-8 -Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager -Djava.util.logging.config.file=/opt/appserv/tomcat/apps/fastPdfService/conf/logging.properties -Djava.endorsed.dirs=/opt/appserv/common/tomcat6_18base endorsed -classpath :/opt/appserv/common/tomcat6_18base/bin/bootstrap.jar -Dcatalina.base=/opt/appserv/tomcat/apps/fastPdfService -Dcatalina.home=/opt/appserv/common/tomcat6_18base -Djava.io.tmpdir=/opt/appserv/tomcat/apps/fastPdfService/temp org.apache.catalina.startup.Bootstrap start

    if (!isHttpd) {

      proc = ("/opt/appserv/common/binGroovy/serverProfilerPs.sh " + pid).execute();

      proc.waitFor();

      String psOutput;
      if (proc.exitValue() == 0) {
        psOutput = proc.in.text;
 
        //System.out.println("\n\n" + pid + "\n\n" + psOutput + "\n\n");
  
        pattern = Pattern.compile('^\\s*\\S+\\s+\\S+\\s+\\S+\\s+\\S+\\s+\\S+\\s+\\S+\\s+\\S+\\s+(.+)\\s*$');

        matcher = pattern.matcher(psOutput);

        if (matcher.matches()) {
 
          commandFriendly = matcher.group(1);

          if (isJava) {
     
            //get down to the app name
            // /opt/appserv/tomcat/apps/fastPdfService/java/bin/java -server -Xms5M
            pattern = Pattern.compile('^/opt/appserv/tomcat/apps/([^/]+)/java.*$');
            matcher = pattern.matcher(commandFriendly);

            if (matcher.matches()) {
          
              commandFriendly = matcher.group(1);

            }

          }
        }
      }
    }

    //have a max length
    if (commandFriendly != null && commandFriendly.length() > 60) {
      commandFriendly = commandFriendly.substring(0,60);
    }

    //high rollers
    System.out.println("Process " + pid + ": " + commandFriendly + ", " + convertMemoryToString(memoryBytes)
      + ", " + (cpuPercentTimesTen/10.0) + "% cpu, " + (memoryPercentTimesTen/10.0) + "% mem");

  }

  //System.out.println(pid + ", " + memory + ", " + cpuPercent + ", " + memoryPercent + ", " + command);


}

DecimalFormat theDecimalFormat = new DecimalFormat("#.##");

System.out.println("Overall CPU: " + cpu + "%, memUsed: " + convertMemoryToString(convertMemory(memoryUsed))
  + " / " + convertMemoryToString(convertMemory(memoryTotal)) + " (" + (theDecimalFormat.format(100d * convertMemory(memoryUsed)/convertMemory(memoryTotal)))  + "%)" + ", Load (1,5,15min) " + load1min
  + ", " + load5min + ", " + load15min);

System.out.println("httpd (" + httpdProcessCount + " processes, " + convertMemoryToString(httpdMemory) + ", " + (httpdPercentCpuTen/10.0) + "% cpu, " + (httpdPercentMemoryTen/10.0) + "% mem)");

System.out.println("java (" + javaProcessCount + " processes, " + convertMemoryToString(javaMemory) + ", " + (javaPercentCpuTen/10.0) + "% cpu, " + (javaPercentMemoryTen/10.0) + "% mem)");


public static String convertMemoryToString(long memory) {

  DecimalFormat decimalFormat = new DecimalFormat("#.##");

  if (memory < 1024) {
    return "" + decimalFormat.format(memory);
  }

  if (memory < 1024*1024) {
    return "" + decimalFormat.format(memory / 1024) + "k";
  }

  if (memory < 1024 * 1024 * 1024) {
    return "" + decimalFormat.format(memory / (1024 * 1024)) + "m";
  }

  return "" + decimalFormat.format(memory / (1024 * 1024 * 1024)) + "g";

}


public static long convertMemory(String memory) {

  memory = memory.toLowerCase();

  if (memory.endsWith("m")) {

    memory = memory.substring(0, memory.length() - 1);
    return java.lang.Double.parseDouble(memory) * 1024 * 1024;

  }

  if (memory.endsWith("k")) {

    memory = memory.substring(0, memory.length() - 1);
    return java.lang.Double.parseDouble(memory) * 1024;

  }

  if (memory.endsWith("g")) {

    memory = memory.substring(0, memory.length() - 1);
    return java.lang.Double.parseDouble(memory) * 1024 * 1024 * 1024;

  }

  if (memory.matches('[0-9\\.]+')) {

    return Long.parseLong(memory);  

  }

  throw new RuntimeException("cant match memory: " + memory);

}