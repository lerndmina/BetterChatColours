# 🎨 BetterChatColours - Chat Input System

## ✨ New Feature: Chat-Based Admin Preset Creation

We've successfully replaced the AnvilGUI system with a much more reliable and user-friendly **chat input system**. This provides a seamless experience for creating gradient presets without the complexity of AnvilGUI.

## 🚀 How It Works

### 1. **Admin Access**

- Players with `chatcolors.admin` permission see a "Create Preset" button in the main menu
- Permission required: `betterchatcolours.admin`

### 2. **Preset Creation Workflow**

#### Step 1: Open Admin Menu

```
/chatcolors → Click "Create Preset" button
```

#### Step 2: Enter Preset Name

- The GUI closes and you get a beautiful chat prompt:

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
✏️ Enter the preset name in chat:
   • Type the name and press Enter
   • Type 'cancel' to return to the menu
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

- Type your preset name (e.g., `rainbow_gradient`)
- Names are automatically cleaned (special characters become underscores)
- Type `cancel` to abort

#### Step 3: Color Selection GUI

- Opens a comprehensive color management interface
- Pre-defined color palette with common colors
- Add custom colors via "Add Custom Color" button
- Remove colors with "Remove Color" buttons
- Real-time gradient preview

#### Step 4: Add Custom Colors (Optional)

Click "Add Custom Color" → Chat prompt appears:

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
🎨 Enter a hex color in chat:
   • Format: #ff0000 or ff0000
   • Type 'cancel' to return to the menu
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

- Type hex colors like `#ff0000` or `ff0000`
- Automatically validates and formats colors
- Type `cancel` to return to color selection

#### Step 5: Edit Existing Colors (Optional)

Click any color in the list → Chat prompt with current color:

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
🎨 Edit hex color (currently: #FF0000):
   • Format: #ff0000 or ff0000
   • Type 'cancel' to return to the menu
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

#### Step 6: Set Permission (Final Step)

Click "Save Preset" → Permission prompt:

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
🔐 Enter permission node (optional):
   • Example: chatcolors.premium
   • Leave empty for no permission
   • Type 'cancel' to return to the menu
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

- Type a permission like `chatcolors.premium` or leave empty
- Press Enter with empty input = no permission required
- Type `cancel` to return to color selection

#### Step 7: Success!

```
✅ Preset 'rainbow_gradient' created with permission: chatcolors.premium
```

Returns to main menu - preset is now available for all users!

## 🛡️ Built-in Validation

### Preset Names

- ✅ Automatically removes special characters
- ✅ Maximum 32 characters
- ✅ Cannot be empty
- ✅ Safe for file storage

### Hex Colors

- ✅ Accepts `#ff0000` or `ff0000` formats
- ✅ Validates actual hex values
- ✅ Automatically converts to uppercase
- ✅ Prevents invalid color codes

### Permissions

- ✅ Accepts standard permission formats
- ✅ Only letters, numbers, dots, hyphens
- ✅ Automatically converts to lowercase
- ✅ Empty = no permission required

## 🚫 Cancellation System

At any point during the process:

- Type `cancel` to abort the current input
- Returns to the previous menu/step
- No data is lost until final save

## 💡 Why Chat Input is Better

### ❌ Problems with AnvilGUI:

- Complex API with breaking changes
- Server compatibility issues
- Limited input validation
- Prone to client-side bugs

### ✅ Benefits of Chat Input:

- 🎯 **100% Reliable** - Works on all Minecraft versions
- 🔒 **Built-in Validation** - Comprehensive input checking
- 🎨 **Beautiful UI** - Professional chat formatting
- 🚫 **Easy Cancellation** - Type 'cancel' anytime
- 🛡️ **No Client Dependencies** - Pure server-side
- 📱 **Works Everywhere** - Compatible with all clients

## 🎮 User Experience

The system feels natural and intuitive:

1. Click button → Get clear instructions
2. Type in chat → Instant validation feedback
3. Automatic return to GUI → Seamless workflow
4. Professional formatting → Looks polished

## 🔧 Technical Implementation

- **ChatInputManager**: Handles all chat input sessions
- **Input Validation**: Comprehensive validation for each input type
- **Session Management**: Tracks active input sessions per player
- **Thread Safety**: Proper main thread execution
- **Event Cancellation**: Chat messages don't appear in public chat during input

This system provides a professional, reliable, and user-friendly experience for admin preset creation without any of the complications that come with AnvilGUI!
