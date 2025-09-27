package g1t1.models.interfaces;

import g1t1.models.users.FaceData;

import java.util.List;

/**
 * Has Faces property to update
 */
public interface HasFaces extends HasProperty {

    public void setFaceData(List<FaceData> faceData);

    // Unfiltered picture
    public void setThumbnail(FaceData thumbnail);
}
