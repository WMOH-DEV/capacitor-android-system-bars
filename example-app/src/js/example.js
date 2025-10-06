import { SystemBarsManager } from 'android-system-bars';

window.testEcho = () => {
    const inputValue = document.getElementById("echoInput").value;
    SystemBarsManager.echo({ value: inputValue })
}
