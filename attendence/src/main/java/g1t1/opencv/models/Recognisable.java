package g1t1.opencv.models;

import g1t1.models.users.FaceData;

/**
 * Interface for all objects that can be recognised by the face recognition service
 */
public interface Recognisable {
    String getName();

    String getRecognitionId();

    FaceData getFaceData();
}
