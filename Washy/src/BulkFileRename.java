import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BulkFileRename {

    public static void main(String[] args) {
        BulkFileRename bulkFileRename = new BulkFileRename();

        File dir = new File("/home/jeremyc/pix/alaska_8-12-2012/cordovaSunset/");
        File outDir = dir;
//        File outDir = new File("/home/jeremyc/pix/alaska_8-12-2012/tram/take1");
        bulkFileRename.bulkRename(dir, outDir);
    }

    public void bulkRename(File inDir, File outDir) {
        Map<Long, File> toRename = getKeymap(inDir);
        List<Long> keys = new ArrayList<Long>(toRename.keySet());
        Collections.sort(keys);
//        Collections.reverse(keys);
        Long idx = 1l;
        for (Long key : keys) {
            moveFile(toRename.get(key), new File(outDir, "frame_" + idx
                    + ".jpg"));
            idx++;
        }
    }

    private void moveFile(File src, File dst) {
        src.renameTo(dst);
    }

    private Map<Long, File> getKeymap(File inDir) {
        Map<Long, File> ret = new HashMap<Long, File>();
        for (File f : inDir.listFiles()) {
            Long key = getKey(f);
            if (key != null) {
                ret.put(key, f);
            }
        }
        return ret;
    }

    private Long getKey(File f) {
        if (f.getName().endsWith(".JPG") || f.getName().endsWith(".jpg")) {
            return Long.parseLong(f.getName().replaceAll("\\.JPG", "")
                    .replaceAll("\\.jpg", "").replaceAll("IMG_", "")
                    .replaceAll("frame_", ""));
        }
        return null;
    }

}
