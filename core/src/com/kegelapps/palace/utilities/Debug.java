package com.kegelapps.palace.utilities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.io.File;
import java.io.FileFilter;

/**
 * Created by keg45397 on 4/21/2016.
 */
public class Debug implements Resettable {
    public static String mSaveFile = "savedTurn";
    public static String mSaveFileExt = ".dat";
    public static int mSaveCounter = -1;

    private boolean mSaveEnabled;
    private boolean mCPUPlay;

    FileFilter filter;

    public Debug() {
        filter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getAbsolutePath().contains("\\"+mSaveFile);
            }
        };
        mSaveEnabled = true;
        mCPUPlay = false;
    }

    public int getLastSaveCounter() {
        FileHandle[] fh = Gdx.files.external(".").list(filter);
        if (fh.length == 0) {
            mSaveCounter = 0;
            return -1;
        }
        for (FileHandle f : fh) {
            int val = Integer.parseInt(f.nameWithoutExtension().substring(mSaveFile.length()));
            if (val > mSaveCounter)
                mSaveCounter = val;
        }
        return mSaveCounter;
    }

    public String getLastSave() {
        String s;
        if (getLastSaveCounter() == -1)
            s = String.format("%s%04d%s", mSaveFile, 0, mSaveFileExt);
        else
            s = String.format("%s%04d%s", mSaveFile, mSaveCounter, mSaveFileExt);
        return Gdx.files.external(s).file().getAbsolutePath();
    }

    public String getNextSave() {
        String s;
        if (getLastSaveCounter() == -1)
            s = String.format("%s%04d%s", mSaveFile, 0, mSaveFileExt);
        else
            s = String.format("%s%04d%s", mSaveFile, mSaveCounter+1, mSaveFileExt);
        return Gdx.files.external(s).file().getAbsolutePath();
    }

    public void disableSaves() {
        mSaveEnabled = false;
    }

    public boolean isSavesEnabled() { return mSaveEnabled;}

    @Override
    public void Reset(boolean newGame) {
        if (!isSavesEnabled())
            return;
        FileHandle[] fh = Gdx.files.external(".").list(filter);
        if (fh.length == 0) {
            return;
        }
        for (FileHandle f : fh) {
            Gdx.files.external(f.path()).delete();
        }
        mSaveCounter = -1;
    }

    public boolean isCPUPlayOnly() {
        return mCPUPlay;
    }

    public void setCPUPlayOnly(boolean val) {
        mCPUPlay = val;
    }
}
