package com.smart_home_system.core;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import com.smart_home_system.devices.Device;
import com.smart_home_system.devices.DeviceFactory;
import com.smart_home_system.devices.ThermoStat;
import com.smart_home_system.tasks.AutomatedTrigger;
import com.smart_home_system.tasks.ScheduledTask;

public class SmartHomeSystem {
	private Map<Integer, Device> devices;
    private List<ScheduledTask> scheduledTasks;
    private List<AutomatedTrigger> automatedTriggers;
    private Set<Integer> ids;
    private Timer timer;
    public int presentInterval(int targetTriggerId) {
    	for(AutomatedTrigger x:automatedTriggers) {
    		if(x.getTriggerId()==targetTriggerId) {
    			return x.getIntervalInSeconds();
    		}
    	}
    	return 0;//if not found
    }
    public SmartHomeSystem() {
        devices = new HashMap<>();
        scheduledTasks = new ArrayList<>();
        automatedTriggers = new ArrayList<>();
        ids=new HashSet<>();
    }
    
    public void addDevice(int id, String type) {
    	if(ids.contains(id)) {
    		 System.out.println("Already Exists!!enter a unique id:");
    		 return;
    	
    	}
        Device device = DeviceFactory.createDevice(id, type);
        ids.add(id);
        devices.put(id, device);
    }

    public void turnOn(int id) {
        Device device = devices.get(id);
        if (device != null) {
            device.turnOn();
        }
    }
    public void turnOff(int id) {
        Device device = devices.get(id);
        if (device != null) {
            device.turnOff();
        }
    }
    public void toggleOnOff(int id,int value) {
    	if(value==0) {
    		turnOff(id);
    	}
    	else if(value==1) {
    		turnOn(id);
    	}
    }
    
    public void scheduleDevice(int deviceId, String time, String action) {
        Device device = devices.get(deviceId);
        if (device != null) {
            ScheduledTask task = new ScheduledTask(device, time, action);
            scheduledTasks.add(task);
            scheduleTaskExecution(task);
        }
        
    }
    
    private void scheduleTaskExecution(ScheduledTask task) {
        // Schedule the task to execute at the specified time
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                task.execute();
            }
        }, parseTime(task.getTime()));
    }

    private long parseTime(String timeString) {
 
        // Define a SimpleDateFormat with the desired time format
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        long millisecondsSinceMidnight=0;
        try {
            // Parse the time string to obtain a Date object
            Date date = sdf.parse(timeString);

            // Calculate the time in milliseconds since midnight
            millisecondsSinceMidnight = date.getTime();
            
          } catch (Exception e) {
            e.printStackTrace();
        }
        return millisecondsSinceMidnight;
        
    }
    
    public void addAutomatedTrigger(String type,String relation,int value, String action,int intervalInSeconds,int triggerId) {
    	int checkDeviceId=0;
    	for(int x:devices.keySet()) {
         	if(devices.get(x).getType().equals(type)) {
         		checkDeviceId=x;
         	}
         }
    	AutomatedTrigger trigger = new AutomatedTrigger(type,relation,value, action,devices.get(checkDeviceId),triggerId,intervalInSeconds);
        automatedTriggers.add(trigger);
        startAutomatedTriggerChecking(intervalInSeconds,trigger);
    }
    
    public void startAutomatedTriggerChecking(int intervalInSeconds,AutomatedTrigger trigger) {
        // Create a timer and schedule it to check triggers every 60 seconds
    	timer = new Timer();
    	TimerTask task=new TimerTask() {
            @Override
            public void run() {
                checkAutomatedTriggers();
            }
        };
        timer.scheduleAtFixedRate(task, 0, intervalInSeconds * 1000);
        System.out.println("new timertask created with interval:"+intervalInSeconds);
        trigger.setTimerTask(task);
    }
    public void changeTriggerCheckingInterval(int targetTriggerId,int newInterval) {
    	boolean cancelled=false;
    	for(AutomatedTrigger x:automatedTriggers) {
    		if(x.getTriggerId()==targetTriggerId) {
    			cancelled=x.getTimerTask().cancel();
    			x.setIntervalInSeconds(newInterval);
    		}
    		if(cancelled==true) {
        		startAutomatedTriggerChecking(newInterval,x);
        	}
    	}
    	
    }
    public void checkAutomatedTriggers() {
        for (AutomatedTrigger trigger : automatedTriggers) {
            if (trigger.isTriggered()) {
                // Execute the action associated with the trigger
                trigger.execute(devices);
            }
        }
        
    }
    
    public void statusReport() {
    	System.out.println("Status Report:");
    	for(int x:devices.keySet()) {
    	if(devices.get(x).getType().equals("thermostat")) {
    		ThermoStat thermostat= (ThermoStat)devices.get(x);
    		System.out.println("\""+devices.get(x).getType()+" is set to  "+thermostat.getTemperature()+"\"");
    		continue;
    	}
    	System.out.println("\""+devices.get(x).getType()+" "+devices.get(x).getId()+" "+devices.get(x).getStatus()+"\"");
    	}
    }
    public void scheduledTask() {
    	System.out.print("[");
    	for(ScheduledTask x:scheduledTasks) {
    	   System.out.print("{device:"+x.getDevice().getId()+", time: \""+x.getTime()+"\", command: \""+ x.getAction()+"\"}");
    	}
    	System.out.print("]");
    }
    public void automatedTriggers() {
    	System.out.print("[");
    	for(AutomatedTrigger x:automatedTriggers) {
    	   System.out.print("{  TriggerId: "+x.getTriggerId()+" condition:"+x.getCondition()+", action: \""+x.getAction()+"\" Time Interval:"+x.getIntervalInSeconds()+"}");
    	}
    	System.out.print("]");
    }
    

}

