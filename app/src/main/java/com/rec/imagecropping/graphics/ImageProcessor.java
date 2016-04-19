package com.rec.imagecropping.graphics;

import java.util.LinkedList;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

import com.rec.imagecropping.graphics.commands.ImageProcessingCommand;


public class ImageProcessor {
	// Singleton
	private static ImageProcessor instance = null;
	private  boolean modified = false;

	private ImageProcessorListener processListener;

	private LinkedList<ImageProcessingCommand> queue;
	private Bitmap savedBitmap;
	private Bitmap lastResultBitmap;

	private Context applicationContext;
	private Handler uiThreadHandler;
	
	
	public  boolean isModified(){
		return modified;
		
	}
	public  void resetModificationFlag(){
		 modified = false;
		
	}
	
	public static ImageProcessor getInstance() {
		if (instance == null) {
			instance = new ImageProcessor();
		}
		return instance;
	}

	private ImageProcessor() {
		queue = new LinkedList<ImageProcessingCommand>();
		workingThread.start();
	}


	private Thread workingThread = new Thread(new Runnable() {
		@Override
		public void run() {
			while (true) {
				ImageProcessingCommand cmd;
				try {
					synchronized (queue) {
						while (queue.isEmpty()) {
							queue.wait();
						}
						cmd = queue.poll();
					}
					// Process command
					onProcessStart();
					lastResultBitmap = cmd.process(savedBitmap);
					cmd = null;
					onProcessEnd();
					modified=true;
				} catch (InterruptedException e) {

					break; // Terminate
				}
			}
		}
		
		private void onProcessStart() {
			if (uiThreadHandler!= null && processListener != null) {
				uiThreadHandler.post(new Runnable() {
					@Override
					public void run() {
						processListener.onProcessStart();
					}
				});
			}
		}

		private void onProcessEnd() {
			// do postprocessing HERE
			// notify UI thread about new bitmap and save results
			if (uiThreadHandler!= null && processListener != null) {
				uiThreadHandler.post(new Runnable() {
					@Override
					public void run() {
						processListener.onProcessEnd(lastResultBitmap);
					}
				});
			}
		}
	});


	public void setBitmap(Bitmap bitmap) {
		this.savedBitmap = bitmap;
	}

	public Bitmap getBitmap() {
		return savedBitmap;
	}


	public void runCommand(ImageProcessingCommand command) {
		conditionallyAddToQueue(command);
	}


	private void conditionallyAddToQueue(ImageProcessingCommand command) {
		synchronized (queue) {
			if (!queue.isEmpty()) {
				ImageProcessingCommand c = queue.getLast();
				if (c.getId().equals(command.getId())) {
					queue.removeLast();
				}
			}
			queue.add(command);
			queue.notify();
		}
	}

	public void save() {
		if (lastResultBitmap != null) {
			if (savedBitmap != lastResultBitmap && savedBitmap != null) {
				savedBitmap.recycle();
			}
			savedBitmap = lastResultBitmap;
			lastResultBitmap = null;
		}
	}

	public ImageProcessorListener getProcessListener() {
		return processListener;
	}

	public void setProcessListener(ImageProcessorListener processListener) {
		this.processListener = processListener;
	}

	public void clearProcessListener() {
		this.processListener = null;
		if (lasResultCanBeRecycled()) {
			lastResultBitmap.recycle();
		}
		this.lastResultBitmap = null;
	}

	private boolean lasResultCanBeRecycled() {
		return lastResultBitmap != savedBitmap && lastResultBitmap != null;
	}

	public Bitmap getLastResultBitmap() {
		return lastResultBitmap;
	}

	public void setLastResultBitmap(Bitmap lastResultBitmap) {
		this.lastResultBitmap = lastResultBitmap;
	}

	public void setApplicationCotnext(Context applicationContext){
		if (this.applicationContext == null){
			uiThreadHandler = new Handler();	
		}
		this.applicationContext = applicationContext;
	}
}
