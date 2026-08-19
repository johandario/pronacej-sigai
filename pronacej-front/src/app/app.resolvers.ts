import { inject } from '@angular/core';
import { NavigationService } from 'app/core/navigation/navigation.service';
import { MessagesService } from 'app/layout/common/messages/messages.service';
import { NotificationsService } from 'app/layout/common/notifications/notifications.service';
import { QuickChatService } from 'app/layout/common/quick-chat/quick-chat.service';
import { ShortcutsService } from 'app/layout/common/shortcuts/shortcuts.service';
import { forkJoin } from 'rxjs';
import { InactivityUserService } from './core/services/inactivityUser.service';
import { PermisoRolUsuarioService } from './modules/seguridad/services/permiso-rol-usuario.service';

export const initialDataResolver = () => {
    const messagesService = inject(MessagesService);
    const navigationService = inject(NavigationService);
    const notificationsService = inject(NotificationsService);
    const quickChatService = inject(QuickChatService);
    const shortcutsService = inject(ShortcutsService);

    const inactivityUserService = inject(InactivityUserService);

    // const permisoRolUsuarioService = inject(PermisoRolUsuarioService);

    // Fork join multiple API endpoint calls to wait all of them to finish
    return forkJoin([
        inactivityUserService.verificarInactividad(),
        navigationService.get(), // obtiene los menus del sistema
        messagesService.getAll(),
        notificationsService.getAll(),
        quickChatService.getChats(),
        shortcutsService.getAll(),        
        // permisoRolUsuarioService.obtenerPermisosUsuario(''),
    ]);
};
