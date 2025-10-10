package g1t1.utils.events.authentication;

import g1t1.models.users.Teacher;

public record OnUserUpdateEvent(Teacher user) {
}
