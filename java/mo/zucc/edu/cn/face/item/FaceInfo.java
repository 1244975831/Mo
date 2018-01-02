package mo.zucc.edu.cn.face.item;

/**
 * Created by a on 2017/10/30.
 */

public class FaceInfo {
    public int no;
    public String facename;
    public byte[] facepic;
    public byte[] faceinfo;
    public byte[] oldfacepic;

    public byte[] getOldfacepic() {
        return oldfacepic;
    }

    public void setOldfacepic(byte[] oldfacepic) {
        this.oldfacepic = oldfacepic;
    }

    public int getNo() {
        return no;
    }

    public void setNo(int no) {
        this.no = no;
    }

    public String getFacename() {
        return facename;
    }

    public void setFacename(String facename) {
        this.facename = facename;
    }

    public byte[] getFacepic() {
        return facepic;
    }

    public void setFacepic(byte[] facepic) {
        this.facepic = facepic;
    }

    public byte[] getFaceinfo() {
        return faceinfo;
    }

    public void setFaceinfo(byte[] faceinfo) {
        this.faceinfo = faceinfo;
    }
}
