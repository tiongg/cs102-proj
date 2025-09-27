package g1t1.models.interfaces;

import g1t1.models.users.FaceData;

/**
 * Has Faces property to update
 */
public interface HasFaces extends HasProperty {

    public void setFaceData(FaceData faceData);

    // Unfiltered picture
    public void setThumbnail(byte[] thumbnail);
}
