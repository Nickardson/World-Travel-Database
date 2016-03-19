package edu.seminolestate.gratzer.wtd.web;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import fi.iki.elonen.NanoHTTPD.DefaultTempFile;
import fi.iki.elonen.NanoHTTPD.TempFile;
import fi.iki.elonen.NanoHTTPD.TempFileManager;
import fi.iki.elonen.NanoHTTPD.TempFileManagerFactory;

/**
 * A TempFileManager Factory which creates TempFileManagers that write to the %temp%/WorldTravelDatabase folder
 * @author Taylor
 * @date 2016-02-14
 */
public class CustomTempFileManagerFactory implements TempFileManagerFactory {
	private class Manager implements TempFileManager {
		private final File tmpdir;

        private final List<TempFile> tempFiles;

        public Manager() {
            this.tmpdir = new File(System.getProperty("java.io.tmpdir"), "WorldTravelDatabase");
            if (!tmpdir.exists()) {
                tmpdir.mkdirs();
            }
            this.tempFiles = new ArrayList<TempFile>();
        }

        @Override
        public void clear() {
            for (TempFile file : this.tempFiles) {
                try {
                    file.delete();
                } catch (Exception ignored) {
//                    LOG.log(Level.WARNING, "could not delete file ", ignored);
                }
            }
            
            this.tempFiles.clear();
        }

        @Override
        public TempFile createTempFile(String filename_hint) throws Exception {
            DefaultTempFile tempFile = new DefaultTempFile(this.tmpdir);
            this.tempFiles.add(tempFile);
            return tempFile;
        }
	}
	
	@Override
	public TempFileManager create() {
		return new Manager();
	}

}
